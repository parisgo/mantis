package io.iohk.ethereum.network

import java.net.{InetSocketAddress, URI}

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import io.iohk.ethereum.network.rlpx.RLPxConnectionHandler
import org.spongycastle.crypto.AsymmetricCipherKeyPair

class PeerManagerActor(nodeKey: AsymmetricCipherKeyPair, nodeStatusHolder: ActorRef) extends Actor with ActorLogging {

  import PeerManagerActor._

  var peers: Map[String, Peer] = Map.empty

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Stop
    }

  override def receive: Receive = {
    case HandlePeerConnection(connection, remoteAddress) =>
      val peer = createPeer(remoteAddress)
      log.info("Peer {} handling incoming peer connection from {}", peer.id, remoteAddress)
      peer.ref ! PeerActor.HandleConnection(connection, remoteAddress)
      sender() ! PeerCreated(peer)

    case ConnectToPeer(uri) =>
      val peer = createPeer(new InetSocketAddress(uri.getHost, uri.getPort))
      peer.ref ! PeerActor.ConnectTo(uri)
      sender() ! PeerCreated(peer)

    case GetPeers =>
      sender() ! peers

    case Terminated(ref) =>
      peers -= ref.path.name
  }

  def createPeer(addr: InetSocketAddress): Peer = {
    val id = addr.toString.filterNot(_ == '/')
    val ref = context.actorOf(PeerActor.props(nodeStatusHolder, _.actorOf(RLPxConnectionHandler.props(nodeKey), "rlpx-connection")), id)
    context watch ref
    val peer = Peer(id, addr, ref)
    peers += id -> peer
    peer
  }

}

object PeerManagerActor {
  def props(nodeKey: AsymmetricCipherKeyPair, nodeStatusHolder: ActorRef): Props =
    Props(new PeerManagerActor(nodeKey, nodeStatusHolder))

  case class HandlePeerConnection(connection: ActorRef, remoteAddress: InetSocketAddress)
  case class ConnectToPeer(uri: URI)

  case class Peer(id: String, remoteAddress: InetSocketAddress, ref: ActorRef)
  case class PeerCreated(peer: Peer)

  case object GetPeers
}
