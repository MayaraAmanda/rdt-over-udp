import java.net.DatagramSocket;

public class EnviandoThread extends Thread implements  Runnable{
    public Integer sleepTime = 1; //segundos
    private DatagramSocket udpSocket;
    private String endereco;

    private Integer porta;
    private Pacote pacote;
    private Boolean foiInterrompido = false;

    public EnviandoThread(DatagramSocket udpSocket, Pacote pacote, String endereco, Integer porta){
        this.udpSocket = udpSocket;
        this.pacote = pacote;
        this.endereco = endereco;
        this.porta = porta;
    }

    @Override
    public void run(){
        // metodo que efetua a retransmissao
        while(!foiInterrompido) {
            System.out.println("[Enviado]     " + pacote.toString());
            Funcoes.send(this.udpSocket, this.pacote.toString(), this.endereco, this.porta);
            try {
                Thread.sleep(this.sleepTime * 1000);
            } catch (InterruptedException e){
                e.printStackTrace();
                break;
            }
        }
    }

    public Pacote getPacket() {
        return this.pacote;
    }

    public void interruptThread(){
        foiInterrompido = true;
        System.out.println("[Reenviando]   Parando... " + this.pacote.toString());
    }

}
