import java.util.ArrayList;
import java.util.List;

public class StringsPacote {

    private static Pacote pacote;

    public static Pacote convert(String pacoteString){

        pacote = new Pacote();

        List<Flags> ListaFlags = new ArrayList<>();

        String[] keyValuePairs = pacoteString.split(",");

        for(int i = 0; i< keyValuePairs.length; i++){
            String[] keyValuePair = keyValuePairs[i].split("=");
            String key = keyValuePair[0];
            String value = keyValuePair[1];

            switch(key){
                case "SYN":
                    if (value.equals("1")){
                        ListaFlags.add(Flags.SYN);
                    }
                    break;
                case "ACK":
                    if (value.equals("1")){
                        ListaFlags.add(Flags.ACK);
                    }
                    break;
                case "FIN":
                    if (value.equals("1")){
                        ListaFlags.add(Flags.FIN);
                    }
                    break;
                case "SEQ":
                    pacote.setNumSeq(Integer.parseInt(value));
                    break;
                case "ACKN":
                    pacote.setNumAck(Integer.parseInt(value));
                    break;
                case "WIN":
                    pacote.setTamJanela(Integer.parseInt(value));
                    break;
                case "ORDEM":
                    pacote.setOrdem(Integer.parseInt(value));
                    break;
                case "DADO":
                    pacote.setData(value);
                    break;
            }
        }
        pacote.setFlags(ListaFlags);
        return pacote;
    }

}
