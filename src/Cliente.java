import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.rmi.MarshalException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Cliente {

    private static DatagramSocket udpSocket;
    private static List<Pacote> pacotesEnviados;
    private static Integer tamJanela = 256;
    private static EstadoConexao estadoConexao;
    private static byte[] pacoteBytes;
    private static Integer inicialNumSeqServ = 0;
    private static Integer atualNumSeq = 0;
    private static String[] mensagem;
    private static Integer ultimoAck;
    private static Boolean isInterrompido = false;
    private static Integer ultimoNumSeq = 0;
    private static Integer ackForadeordem = 1;
    private static Integer seq = 0;
    private static String valor, valor2;


    public static void main(String[] args) {

        pacotesEnviados = new ArrayList<>();
        estadoConexao = EstadoConexao.HANDSHAKE_1;
        try {
            udpSocket = new DatagramSocket(Funcoes.getClientPort());
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        System.out.println("Cliente ouvindo...");

        mensagem = new String[Funcoes.getMaxMessageSize()+1];
        ultimoAck = 0;
        receivingThread.start();
    }

    private static Thread receivingThread = new Thread(new Runnable() {

        @Override
        public void run() {
            Pacote primeiroPacoteSyn = new Pacote();
            primeiroPacoteSyn.setSynFlag();// adiciono a flag syn na lista
            atualNumSeq = Funcoes.getRandomNumber(Funcoes.getMenorSeqNum(), Funcoes.getMaxSeqNum()); // gero um numero de sequencia aleatorio
            primeiroPacoteSyn.setNumSeq(atualNumSeq); // seto o numero no pacote que sera transmitido
            System.out.println("[Handshake]    Enviando SYN inicial para o servidor.");
            Funcoes.send(udpSocket, primeiroPacoteSyn.toString(), Funcoes.getServerAddress(), Funcoes.getServerPort()); //envio o pacote para o ip e porta e porta do servidor
            estadoConexao = EstadoConexao.HANDSHAKE_2; // altero o estado da conexão para handshake 2.

            while(!isInterrompido) {
                pacoteBytes = new byte[Funcoes.getMaxPacketSize()];// crio um pacote de bytes
                DatagramPacket pacoteRecebido = new DatagramPacket(pacoteBytes, pacoteBytes.length);
                //Pacote pacoteArmazenado = new Pacote();
                try {
                    udpSocket.receive(pacoteRecebido);
                    byte[] pacoteBytesValidos = new byte[pacoteRecebido.getLength()];
                    System.arraycopy(pacoteRecebido.getData(), 0, pacoteBytesValidos, 0, pacoteRecebido.getLength());//arraycopy copia os dados do array do pacote recebido
                    String pacoteString = new String(pacoteBytesValidos);
                    Pacote pacote = StringsPacote.convert(pacoteString);
                    //listaPacotes.add(pacote.getOrdem(), pacote.getData());
                        int randomNum = ThreadLocalRandom.current().nextInt(0, 4); //gera um inteiro aleatorio
                        if (randomNum % 3 == 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("[Recebido]     " + pacoteString);
                            Runnable r = new processingThread(pacote);
                            new Thread(r).start(); //insere o pacote na mensagem que sera retornada no final
                        } else if (randomNum % 3 == 1) {
                            System.out.println("[Recebido]     " + pacoteString);
                            Runnable r = new processingThread(pacote);
                            new Thread(r).start();
                        } else {
                            continue; //descarta o pacote
                        }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    static class processingThread extends Thread implements Runnable {
        Pacote pacote;
        public processingThread(Pacote pacote){
            this.pacote = pacote;
        }
        @Override
        public void run(){
            if (estadoConexao == EstadoConexao.HANDSHAKE_2) {
                if (pacote.hasSynFlagSet() && pacote.hasAckFlagSet() && pacote.getNumAck().equals(atualNumSeq + 1)) {
                    System.out.println("[Handshake]    Recebeu SYN + ACK do servidor, enviando ACK");
                    atualNumSeq += 1;
                    inicialNumSeqServ = pacote.getNumSeq();
                    System.out.println("[Informação]   Numero de sequencia inicial: " + inicialNumSeqServ);

                    Pacote resposta = new Pacote();
                    resposta.setAckFlag();
                    resposta.setNumAck(pacote.getNumSeq() + 1);
                    resposta.setTamJanela(1);

                    Funcoes.send(udpSocket, resposta.toString(), Funcoes.getServerAddress(), Funcoes.getServerPort());
                    estadoConexao = EstadoConexao.ESTABELECIDA;
                }

            } else if (estadoConexao == EstadoConexao.ESTABELECIDA || estadoConexao == EstadoConexao.FINALIZAR_1) {
                if (pacote.hasFinFlagSet() && estadoConexao != EstadoConexao.FINALIZAR_1){
                    System.out.println("[Finalizar]    Pacote FIN recebido, aguardando que todas as mensagens sejam recebidas");
                    estadoConexao = EstadoConexao.FINALIZAR_1;
                    ultimoNumSeq = pacote.getNumSeq(); //atribuo o numero de sequencia atual a variavel ultimoNumSeq
                } else if (pacote.getNumSeq() - inicialNumSeqServ < Funcoes.getMaxMessageSize() && mensagem[pacote.getNumSeq() - inicialNumSeqServ] == null) {//verifica se todos os pacotes foram recebidos
                    seq = pacote.getNumSeq() - inicialNumSeqServ;
                    ackForadeordem = pacote.getOrdem()+1;

                    mensagem[seq] = pacote.getData();
                    if (ultimoAck + 1 == pacote.getNumSeq() - inicialNumSeqServ) {//significa que existe pacote ausente
                        for (;;) {
                            if (mensagem[ultimoAck + 1] != null) {
                                ultimoAck += 1;
                            } else {
                                break;
                            }
                        }
                    }
                    Pacote novoPacote = new Pacote();
                    novoPacote.setAckFlag();
                    novoPacote.setNumAck(ultimoAck + inicialNumSeqServ + 1); //ack de recebimento
                    novoPacote.setTamJanela(tamJanela);
                    Funcoes.send(udpSocket, novoPacote.toString(), Funcoes.getServerAddress(), Funcoes.getServerPort());
                    System.out.println("[Informação]   Pacote perdido, solicitando retransmissão");
                }
                if (estadoConexao == EstadoConexao.FINALIZAR_1){
                    if (ultimoAck == ultimoNumSeq - inicialNumSeqServ - 1){
                        Pacote novoPacote = new Pacote();
                        novoPacote.setFinFlag();
                        novoPacote.setAckFlag();
                        novoPacote.setNumAck(ultimoNumSeq + 1);
                        System.out.println("[Informação]   Numero de sequencia final: " + ultimoNumSeq);
                        System.out.println("[Informação]   Caracteres recebidos: " + (ultimoNumSeq - inicialNumSeqServ-1));
                        System.out.println("[Finalizar]    Recebeu todos os pacotes, enviando FIN + ACK");
                        Funcoes.send(udpSocket, novoPacote.toString(), Funcoes.getServerAddress(), Funcoes.getServerPort());

                        System.out.println("Conexão fechada");
                        System.out.print("Mensagem recebida: ");
                        for(int i=1; i<ultimoAck+1; i++){
                            if (mensagem[i] != null){
                                System.out.print(mensagem[i]);
                            } else {
                                break;
                            }
                        }
                        isInterrompido = true;
                        System.exit(0);
                    }
                }

            }

        }
    }
}
