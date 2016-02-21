package io.scalechain.blockchain.api.command.mining

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.domain.{RpcError, RpcRequest, RpcResult}

/*
  CLI command :
    # Submit the following block with the workid, “test”.

    bitcoin-cli -testnet submitblock 02000000df11c014a8d798395b5059c\
      722ebdf3171a4217ead71bf6e0e99f4c7000000004a6f6a2db225c81e77773f6\
      f0457bcb05865a94900ed11356d0b75228efb38c7785d6053ffff001d005d437\
      0010100000001000000000000000000000000000000000000000000000000000\
      0000000000000ffffffff0d03b477030164062f503253482fffffffff0100f90\
      29500000000232103adb7d8ef6b63de74313e0cd4e07670d09a169b13e4eda2d\
      650f529332c47646dac00000000 \
      '{ "workid": "test" }'

  CLI output :
    duplicate

  Json-RPC request :
    {"jsonrpc": "1.0", "id":"curltest", "method": "submitblock", "params": [] }

  Json-RPC response :
    {
      "result": << Same to CLI Output >> ,
      "error": null,
      "id": "curltest"
    }
*/

case class SubmitBlockResult(
) extends RpcResult

/** SubmitBlock: accepts a block, verifies it is a valid addition to the block chain, and
  * broadcasts it to the network.
  *
  * Extra parameters are ignored by Bitcoin Core but may be used by mining pools or other programs.
  *
  * https://bitcoin.org/en/developer-reference#submitblock
  */
object SubmitBlock extends RpcCommand {
  def invoke(request : RpcRequest) : Either[RpcError, RpcResult] = {
    // TODO : Implement
    assert(false)
    Right(null)
  }
  def help() : String =
    """submitblock "hexdata" ( "jsonparametersobject" )
      |
      |Attempts to submit new block to network.
      |The 'jsonparametersobject' parameter is currently ignored.
      |See https://en.bitcoin.it/wiki/BIP_0022 for full specification.
      |
      |Arguments
      |1. "hexdata"    (string, required) the hex-encoded block data to submit
      |2. "jsonparametersobject"     (string, optional) object of optional parameters
      |    {
      |      "workid" : "id"    (string, optional) if the server provided a workid, it MUST be included with submissions
      |    }
      |
      |Result:
      |
      |Examples:
      |> bitcoin-cli submitblock "mydata"
      |> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "submitblock", "params": ["mydata"] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
    """.stripMargin
}

