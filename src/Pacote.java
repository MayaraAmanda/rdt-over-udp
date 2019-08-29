import java.util.ArrayList;
import java.util.List;

public class Pacote {

    List<Flags> ListaFlags;
    Integer numSeq;
    Integer numAck;
    Integer tamJanela;
    String data;
    Integer ordem;

    public Pacote(List<Flags> ListaFlags, int numSeq, int numAck, int tamJanela, int ordem, String data ){
        this.ListaFlags = ListaFlags;
        this.numSeq = numSeq;
        this.numAck = numAck;
        this.tamJanela = tamJanela;
        this.data = data;
        this.ordem = ordem;
    }

    public Pacote(){
        this.ListaFlags  = new ArrayList<Flags>();
        this.numSeq = 0;
        this.numAck = 0;
        this.tamJanela = 0;
        this.data = null;
        this.ordem = 0;
    }

    public Boolean hasSynFlagSet(){
        if (ListaFlags.contains(Flags.SYN)){
            return true;
        }
        return false;
    }

    public Boolean hasAckFlagSet(){
        if (ListaFlags.contains(Flags.ACK)){
            return true;
        }
        return false;
    }

    public Boolean hasFinFlagSet(){
        if (ListaFlags.contains(Flags.FIN)){
            return true;
        }
        return false;
    }

    public Boolean hasResetFlagSet(){
        if (ListaFlags.contains(Flags.RST)){
            return true;
        }
        return false;
    }

    public void setFlags(List<Flags> flags){
        this.ListaFlags = flags;
    }

    public Integer getTamJanela() {
        return tamJanela;
    }

    public void setTamJanela(Integer tamJanela) {
        this.tamJanela = tamJanela;
    }

    public String getData(){
        return this.data;
    }

    public void setSynFlag(){
        if (!ListaFlags.contains(Flags.SYN)){
            ListaFlags.add(Flags.SYN);
        }
    }

    public void setAckFlag(){
        if (!ListaFlags.contains(Flags.ACK)){
            ListaFlags.add(Flags.ACK);
        }
    }

    public void setFinFlag(){
        if (!ListaFlags.contains(Flags.FIN)){
            ListaFlags.add(Flags.FIN);
        }
    }

    public Integer getNumSeq() {
        return numSeq;
    }

    public void setNumSeq(Integer numSeq) {
        this.numSeq = numSeq;
    }

    public Integer getNumAck() {
        return numAck;
    }

    public void setNumAck(Integer numAck) {
        this.numAck = numAck;
    }

    public void setData(String data){
        this.data = data;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    @Override
    public String toString(){
        String packetString = "";

        if (this.ListaFlags.contains(Flags.SYN)){//flag syn
            packetString += "SYN=1,";
        } else {
            packetString += "SYN=0,";
        }

        if (this.ListaFlags.contains(Flags.FIN)){ //flag fin
            packetString += "FIN=1,";
        } else {
            packetString += "FIN=0,";
        }

        if (this.ListaFlags.contains(Flags.ACK)){ //flag ack
            packetString += "ACK=1,";
        } else {
            packetString += "ACK=0,";
        }

        packetString += "SEQ=" + this.numSeq + ",";
        packetString += "ACKN=" + this.numAck + ",";
        packetString += "WIN=" + this.tamJanela + ",";
        packetString += "ORDEM=" + this.ordem + ",";
        packetString += "DADO=" + this.data;

        return packetString;
    }
}
