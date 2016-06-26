package io.scalechain.blockchain.cli

import java.util

import io.scalechain.blockchain.chain.{BlockMining, Blockchain}
import io.scalechain.blockchain.net.PeerCommunicator
import io.scalechain.blockchain.proto.{CoinbaseData, Hash, Block}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.util.Utils
import io.scalechain.wallet.Wallet
import org.slf4j.LoggerFactory

import scala.util.Random

case class CoinMinerParams(InitialDelayMS : Int, HashDelayMS : Int, MaxBlockSize : Int )
/**
  * Created by kangmo on 3/15/16.
  */
object CoinMiner {
  var theCoinMiner : CoinMiner = null

  def create(minerAccount : String, wallet : Wallet, chain : Blockchain, peerCommunicator: PeerCommunicator, params : CoinMinerParams) = {
    theCoinMiner = new CoinMiner(minerAccount, wallet, chain, peerCommunicator, params)
    theCoinMiner.start()
    theCoinMiner
  }

  def get = {
    assert(theCoinMiner != null)
    theCoinMiner
  }
}


class CoinMiner(minerAccount : String, wallet : Wallet, chain : Blockchain, peerCommunicator: PeerCommunicator, params : CoinMinerParams) {
  private val logger = LoggerFactory.getLogger(classOf[CoinMiner])

  // For every 10 seconds, create a new block template for mining a block.
  // This means that transactions received within the time window may not be put into the mined block.
  val MINING_TRIAL_WINDOW_MILLIS = 10000
  val PREMINE_BLOCKS = 5;

  val blockMining = new BlockMining( chain.txPool, chain )

  def start() : Unit = {

    val thread = new Thread {
      override def run {
        logger.info(s"Miner started. Params : ${params}")
        val random = new Random(System.currentTimeMillis())

        // TODO : Need to eliminate this code.
        // Sleep for one minute to wait for each peer to start.
        Thread.sleep(params.InitialDelayMS)

        // Step 1 : Set the minder's coin address to receive block mining reward.
        val minerAddress = wallet.getReceivingAddress(minerAccount)

        var nonce : Int = 1

        while(true) { // This thread loops forever.
          nonce += 1
          // Randomly sleep from 100 to 200 milli seconds. On average, sleep 60 seconds.
          // Because current difficulty(max hash : 00F0.. ) is to find a block at the probability 1/256,
          // We will get a block in (100ms * 256 = 25 seconds) ~ (200 ms * 256 = 52 seconds)

          var sleep = true
          if (params.InitialDelayMS % 100 == 76) {
            if ( chain.getBestBlockHeight() < 10 ) {
              sleep = false
            }
          }
          if (sleep) {
            Thread.sleep(random.nextInt(params.HashDelayMS))
          }

          val COINBASE_MESSAGE = CoinbaseData(s"height:${chain.getBestBlockHeight() + 1}, ScaleChain by Kwanho, Chanwoo, Kangmo.")
          // Step 2 : Create the block template
          val blockTemplate = blockMining.getBlockTemplate(COINBASE_MESSAGE, minerAddress, params.MaxBlockSize)
          val bestBlockHash = chain.getBestBlockHash()
          if (bestBlockHash.isDefined) {
            // Step 3 : Get block header
            val blockHeader = blockTemplate.getBlockHeader(Hash(bestBlockHash.get.value))
            val startTime = System.currentTimeMillis()
            var blockFound = false;

            // Step 3 : Loop until we find a block header hash less than the threshold.
//            do {
              // TODO : BUGBUG : Need to use chain.getDifficulty instead of using a fixed difficulty
//              val blockHashThreshold = Hash("00F0000000000000000000000000000000000000000000000000000000000000")
              val blockHashThreshold = Hash("0F00000000000000000000000000000000000000000000000000000000000000")

              val newBlockHeader = blockHeader.copy(nonce = nonce)
              val newBlockHash = newBlockHeader.hash

              if (Hash.isLessThan(newBlockHash, blockHashThreshold)) {
                // Check the best block hash once more.
                if ( bestBlockHash.get.value == chain.getBestBlockHash().get.value ) {
                  // Step 5 : When a block is found, create the block and put it on the blockchain.
                  // Also propate the block to the peer to peer network.
                  val block = blockTemplate.createBlock(newBlockHeader, nonce)
                  chain.putBlock(Hash(newBlockHash.value), block)
                  peerCommunicator.propagateBlock(block)
                  blockFound = true
                  logger.info(s"Block Mined.\n hash : ${newBlockHash}, block : ${block}\n\n")
                }
              }
 //           } while (System.currentTimeMillis() - startTime < MINING_TRIAL_WINDOW_MILLIS && !blockFound)
          } else {
            logger.error("The best block hash is not defined yet.")
          }
        }
      }
    }
    thread.start
  }
}
