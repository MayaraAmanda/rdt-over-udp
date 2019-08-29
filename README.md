# rdt-over-udp
Trabalho desenvolvido na matéria de redes de computadores (Enunciado)

O trabalho (T1) compreende a implementação de funções de um protocolo RDT sobre o protocolo UDP. Algumas características são: reconhecimento de segmentos, ordenação e tratamento de perda de pacotes. A linguagem de programação é de escolha do aluno.

Os requisitos para avaliação serão:
1 - Implementação de Handshake (SYN, SYN-ACK e ACK)
2 - Implementação de Finalização de conexão (FIN)
3 - Números de sequência para segmentos e ACKs (que devem ser negociados por ambos - origem e destino - no handshake)
4 - Reconhecimento de segmentos (ACK)
5 - Escolha de uma das implementação de rdt para tratamento de pacotes perdidos ou fora de ordem. 
    Opções:
      Go-Back-N (GBN)
      Repetição Seletiva
6 - Comunicação cliente-servidor
7 - Considerar somente 1 cliente e 1 servidor
8 - Realizar a simulação de 
    Perda de pacotes
    ACKs duplicados
    Recebimento de segmentos fora de ordem  
