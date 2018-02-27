package io.iohk.ethereum.jsonrpc

import org.json4s.JsonAST.JValue

case class JsonRpcError(code: Int, message: String, data: Option[JValue])

// scalastyle:off magic.number
// scalastyle:off public.methods.have.type
object JsonRpcErrors {
  val ParseError = JsonRpcError(-32700, "An error occurred on the server while parsing the JSON text", None)
  val InvalidRequest = JsonRpcError(-32600, "The JSON sent is not a valid Request object", None)
  val MethodNotFound = JsonRpcError(-32601, "The method does not exist / is not available", None)
  def InvalidParams(msg: String = "Invalid method parameters") = JsonRpcError(-32602, msg, None)
  val InternalError = JsonRpcError(-32603, "Internal JSON-RPC error", None)
  def LogicError(msg: String) = JsonRpcError(-32000, msg, None)
  val AccountLocked = LogicError("account is locked or unknown")

  // Using the recommendation from https://github.com/ethereum/wiki/wiki/JSON-RPC-Error-Codes-Improvement-Proposal
  // Error Code "2", "Action not allowed" could be a candidate here
  final val ConsensusIsNotEthash = JsonRpcError(200, "The consensus algorithm is not Ethash", None)
}
