import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Servidor {

    private static DatagramSocket udpSocket;
    private static String mensagemArquivo = "src/mensagem";
    private static List<Pacote> pacotesEnviados;
    private static byte[] pacotesBytes;
    private static Integer numSeqAtual = 0;
    private static Integer numSeqInicial = 0;
    private static EstadoConexao estadoConexaoAtual;
    private static String msgString;
    private static Integer tamJanelaAtual;
    private static ArrayList<EnviandoThread> janelaAtual;
    private static Integer sleepTime = 1; //seconds
    private static Integer ultimoAck;
    private static Boolean foiInterrompido = false;
    private static Integer valor = 0;
    private static Integer i = 0;

    public static void main(String[] args){

        pacotesEnviados = new ArrayList<>();
        estadoConexaoAtual = EstadoConexao.HANDSHAKE_1;

        try {
            udpSocket = new DatagramSocket(Funcoes.getServerPort());
        } catch(SocketException e){
            e.printStackTrace();
            return;
        }

        System.out.println("Servidor ouvindo...");
        numSeqAtual = Funcoes.getRandomNumber(Funcoes.getMenorSeqNum(), Funcoes.getMaxSeqNum());

        tamJanelaAtual = 1;
        janelaAtual = new ArrayList<EnviandoThread>();
        ultimoAck = 0;

        receivingThread.start();
    }

    private static Thread receivingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(!foiInterrompido) {
                for (; ; ) {
                    pacotesBytes = new byte[Funcoes.getMaxPacketSize()];
                    DatagramPacket pacoteRecebido = new DatagramPacket(pacotesBytes, pacotesBytes.length);
                    try {
                        udpSocket.receive(pacoteRecebido);
                        byte[] pacoteValidoBytes = new byte[pacoteRecebido.getLength()];
                        System.arraycopy(pacoteRecebido.getData(), 0, pacoteValidoBytes, 0, pacoteRecebido.getLength());
                        String pacoteString = new String(pacoteValidoBytes);
                        System.out.println("[Recebido]     " + pacoteString);
                        System.out.println("[Informação]    Pacote perdido, reetransmitindo");
                        Pacote pacote = StringsPacote.convert(pacoteString);

                        boolean pacoteNaJanela = false;
                        for (int i = 0; i < janelaAtual.size(); i++) {
                            EnviandoThread thread = janelaAtual.get(i);
                            if (thread.getPacket().getNumSeq().equals(pacote.getNumAck() - 1)) {
                                janelaAtual.remove(i);
                                thread.interruptThread();
                                pacoteNaJanela = true;
                            }
                        }

                        if (pacoteNaJanela || estadoConexaoAtual == EstadoConexao.HANDSHAKE_1) {
                            if (numSeqAtual < pacote.getNumAck()) {
                                numSeqAtual = pacote.getNumAck();
                            }
                            Thread r = new processingThread(pacote);
                            r.run();
                        } else {
                            // descarta o pacote
                            continue;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });


    static class processingThread extends Thread implements Runnable {
        Pacote pacote;

        public processingThread(Pacote pacote) {
            this.pacote = pacote;
        }

        @Override
        public void run() {
            if (estadoConexaoAtual == EstadoConexao.HANDSHAKE_1) {
                if (pacote.hasSynFlagSet()) {
                    System.out.println("[Handshake]    Pacote SYN recebido do cliente.");
                    System.out.println("[Handshake]    Enviando SYN + ACK para o cliente.");
                    Pacote ansPacket = new Pacote();
                    ansPacket.setSynFlag();
                    ansPacket.setAckFlag();
                    ansPacket.setNumAck(pacote.getNumSeq() + 1);
                    ansPacket.setNumSeq(numSeqAtual);
                    estadoConexaoAtual = EstadoConexao.HANDSHAKE_2;
                    send(udpSocket, ansPacket, Funcoes.getClientAddress(), Funcoes.getClientPort());
                }
            } else if (estadoConexaoAtual == EstadoConexao.HANDSHAKE_2) {
                if (pacote.hasAckFlagSet() && pacote.getNumAck().equals(numSeqAtual)) {
                    numSeqInicial = numSeqAtual;
                    estadoConexaoAtual = EstadoConexao.ESTABELECIDA;
                    System.out.println("[Handshake]    Pacote ACK recebido do cliente.");
                    System.out.println("[Estabelecida] Handshake completo, enviando primeiro byte de dados..");
                    File arquivo = new File(mensagemArquivo);
                    try {
                        FileInputStream fis = new FileInputStream(arquivo);
                        byte[] data = new byte[(int) arquivo.length()];
                        fis.read(data);
                        fis.close();
                        msgString = new String(data, "UTF-8");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        super.interrupt();
                        foiInterrompido = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        super.interrupt();
                        foiInterrompido = true;
                    }

                    Pacote primeiroPacote = new Pacote();
                    primeiroPacote.setSynFlag();
                    primeiroPacote.setNumSeq(numSeqAtual);
                    String enviarDado = msgString.charAt(numSeqAtual - numSeqInicial) + ""; //retorna 1 caractere da string
                    primeiroPacote.setData(enviarDado);
                    send(udpSocket, primeiroPacote, Funcoes.getClientAddress(), Funcoes.getClientPort());
                }
            } else if (estadoConexaoAtual == EstadoConexao.ESTABELECIDA) {
                if (pacote.getTamJanela() >= 0){
                    tamJanelaAtual = pacote.getTamJanela();
                }
                if ((numSeqAtual - numSeqInicial) < msgString.length()) {//significa que ainda tenho caracteres a ser enviado
                    Pacote novoPacote = new Pacote();
                    novoPacote.setSynFlag();
                    novoPacote.setNumSeq(numSeqAtual);
                    valor = numSeqAtual-numSeqInicial;

                    //if(valor==3) {
                      //  novoPacote.setData(msgString.charAt(valor + 1) + "");
                        //novoPacote.setOrdem(valor + 1);
                    //}else if(valor==4) {
                      //  novoPacote.setData(msgString.charAt(valor-1) + "");
                        //novoPacote.setOrdem(valor-1);
                    //}else{
                        novoPacote.setData(msgString.charAt(valor) + "");
                        novoPacote.setOrdem(valor);
                    //}
                    send(udpSocket, novoPacote, Funcoes.getClientAddress(), Funcoes.getClientPort());
                } else {
                    System.out.println("[Finalizar]    Mensagem finalizada, enviando FIN");
                    estadoConexaoAtual = EstadoConexao.FINALIZAR_1;
                    Pacote pacoteFinal = new Pacote();
                    pacoteFinal.setFinFlag();
                    pacoteFinal.setNumSeq(numSeqAtual);
                    send(udpSocket, pacoteFinal, Funcoes.getClientAddress(), Funcoes.getClientPort());
                }
            } else if (estadoConexaoAtual == EstadoConexao.FINALIZAR_1) {
                if (pacote.hasFinFlagSet() && pacote.hasAckFlagSet()) {
                    estadoConexaoAtual = EstadoConexao.FINALIZAR_2;
                    Pacote novoPacote = new Pacote();
                    novoPacote.setAckFlag();
                    novoPacote.setNumAck(pacote.getNumSeq() + 1);
                    System.out.println("[Finalizar]    FIN + ACK recebido, enviando ACK");
                    Funcoes.send(udpSocket, novoPacote.toString(), Funcoes.getServerAddress(), Funcoes.getServerPort());
                    System.out.println("Encerrando conexao..");

                    for(;;){
                        if (janelaAtual.size() == 0){
                            foiInterrompido = true;
                            System.exit(0);
                        } else {
                            try {
                                Thread.sleep(2000);
                            } catch(InterruptedException e){
                                e.printStackTrace();
                                foiInterrompido = true;
                                System.exit(0);
                            }
                        }
                    }
                }
            } else {
                super.interrupt();
                foiInterrompido = true;
            }
        }

    }


    public static void send(DatagramSocket socket, Pacote message, String address, Integer port){
        for(;;) {
            if (tamJanelaAtual > janelaAtual.size()) {
                EnviandoThread sendingThread = new EnviandoThread(socket, message, address, port);
                janelaAtual.add(sendingThread);
                new Thread(sendingThread).start();
                break;
            }
            try {
                Thread.sleep(sleepTime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
