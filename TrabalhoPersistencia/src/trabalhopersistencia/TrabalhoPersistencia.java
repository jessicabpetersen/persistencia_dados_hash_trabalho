/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalhopersistencia;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.JOptionPane;

/**
 *
 * @author Jéssica Petersen
 */
public class TrabalhoPersistencia {
    static RandomAccessFile arquivo;
    static Usuario usuario;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
       int menu = -1;
       criaArquivo();
        
       try{
           while(menu != 0){
               menu = Integer.parseInt(JOptionPane.showInputDialog("Menu:\n"
                       + "1 - adicionar\n" 
                       + "2 - remover\n" 
                       + "3 - alterar\n"
                       + "4 - consultar\n"
                       + "5 - Mostrar todos\n"
                       + "6 - Mostrar todos de um determinado hash\n" 
                       + "0 - sair"));
               switch(menu){
                   case 1:
                       adicionaUsuario();  
                       break;
                   case 2:
                       removerUsuario();
                       break;
                   case 3:
                       alterarUsuario();    
                       break;
                   case 4:
                       consultar();     
                       break;
                   case 5:
                       mostrarTudo(0, "");   
                       break;
                   case 6:
                       int hash = Integer.parseInt(JOptionPane.showInputDialog("Digite o hash desejado (de 0 a 27)"));
                        if(hash >= 0 && hash < 28){
                           String mostrar = "Hash: "+ hash;
                           mostrar = mostrarPorHash(hash*136, ""); 
                           JOptionPane.showMessageDialog(null, mostrar);
                        }else{
                            JOptionPane.showMessageDialog(null, "Hash inválido");
                        }
                       break;
               }
           }
       }catch(Exception e){
           System.out.println(e);
       }  
    }
    /**
     * Cria os 28hash vazios
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void criaArquivo() throws FileNotFoundException, IOException{                 
        arquivo = new RandomAccessFile("trabalhoPersistencia.csv", "rw" );
        arquivo.seek(0);
        for (int i = 0; i < 28; i++) {
            arquivo.writeInt(-1);
            arquivo.writeChars(String.format("%1$60s", ""));
            arquivo.writeDouble(0);
            arquivo.writeInt(-1);
        }
    }
    
    /**
     * Adiciona um usuário
     * @throws IOException 
     */
    public static void adicionaUsuario() throws IOException{                            
        usuario = new Usuario();
        cadastraInfoUsuario(usuario, false);
        usuario.setProximo(-1);
        gravar(usuario);
    }
    
    
    
    /**
     * Método que cadastra as informacoes no objeto usuário
     * @param usuario 
     */
    public static void cadastraInfoUsuario(Usuario usuario, boolean alterar) throws IOException{                    
        boolean certo;
        if(!alterar){
            do{
                usuario.setCodigo(Integer.parseInt(JOptionPane.showInputDialog("Informe o código do usuário")));
                certo = verificarCadastrado(usuario.getCodigo());
                if(certo){
                    JOptionPane.showMessageDialog(null, "Código já castrado");
                }
            }while(certo);
        }
        certo = false;
        while(!certo){
            String nome = JOptionPane.showInputDialog("Informe o nome do usuário");
            if (nome.length() < 31) {
                if (nome.length() < 30) {
                    nome = completaString(nome);
                }
                usuario.setNome(nome);
                certo = true;
            }else{
                JOptionPane.showMessageDialog(null, "Nome do usuário muito grande!");
            }
        }
        usuario.setSalario(Double.parseDouble(JOptionPane.showInputDialog("Informe o salário")));
        certo = false;
        while(!certo){
            String qualificacao = JOptionPane.showInputDialog("Informe a qualificacao do usuário");
            if (qualificacao.length() < 31) {
                if (qualificacao.length() < 30) {
                    qualificacao = completaString(qualificacao);
                }
                usuario.setQualificacao(qualificacao);
                certo = true;
            }else{
                JOptionPane.showMessageDialog(null, "Qualificacao muito grande!");
            }
        }
    }
    
    /**
     * Retorna a posicao inicial(hash inicial) que o código vai pertencer
     * @param codigo
     * @return posicao
     * @throws IOException 
     */
    public static int hashInicialCodigo(int codigo) throws IOException{                                  
        int hash = codigo%28;
        return (hash*136);
    }
    
    /**
     * Realiza o processo de inserção
     * @param usuario
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void gravar(Usuario usuario) throws FileNotFoundException, IOException{   
        int posicaoInicialArquivo = hashInicialCodigo(usuario.getCodigo());
        arquivo.seek(posicaoInicialArquivo);
        int codigo = arquivo.readInt();
        if (codigo == -1) {
            verificaOndeGravar(posicaoInicialArquivo +132, posicaoInicialArquivo);
        }else{ // se já possui o primeiro hash -- procurar nas proximas posições
            arquivo.seek(posicaoInicialArquivo+132);
            int proximoCodigo = arquivo.readInt();
            if(proximoCodigo == -1){
                arquivo.seek(posicaoInicialArquivo+132);
                arquivo.writeInt((int)arquivo.length());
                gravarPosicoesProximas((int)arquivo.length());
            }else{
                gravarPosicoesProximas(proximoCodigo);
            }
        }
    }
    
    /**
     * Verifica se o código que está sendo cadastrado já foi cadastrado anteriormente
     * @param codigoProcurado int
     * @return bool
     */
    public static boolean verificarCadastrado(int codigoProcurado) throws IOException{   
        boolean achou = false;
        int posicao = hashInicialCodigo(codigoProcurado);
        arquivo.seek(posicao);
        int codigoLeitura = arquivo.readInt();
        if (codigoLeitura == codigoProcurado) {
            return true;
        }else{
            achou = verificaProximosCadastrados(posicao+132, codigoProcurado, achou);
        }
        
        return achou;
    }
    
    /**
     * Faz a leitura para veirifcar se o código que está sendo cadastrado já foi cadastrado anteriormente;
     * @param proximaLeitura
     * @param codigoProcurado
     * @return boolean
     * @throws IOException 
     */
    public static boolean verificaProximosCadastrados(int proximaLeitura, int codigoProcurado, boolean achou) throws IOException{   
        arquivo.seek(proximaLeitura);
        int proximaPosicao = arquivo.readInt();
        if(proximaPosicao != -1 ){
            arquivo.seek(proximaPosicao);
            int codigoLeituro = arquivo.readInt();
            if(codigoLeituro == -1){
                arquivo.seek(proximaPosicao+132);
                int proxP = arquivo.readInt();
                if(proxP == -1){
                    return false;
                }else{
                    achou = verificaProximosCadastrados(proximaPosicao+132, codigoProcurado, achou);
                }
            }else{
                if(codigoLeituro != codigoProcurado){
                    achou = verificaProximosCadastrados(proximaPosicao+132, codigoProcurado, achou);
                }else{
                    return true;
                }
            }
        }
        return achou;
    }
    /**
     * Completa a string para ficar com 30 char
     * @param completar
     * @return 
     */
    public static String completaString(String completar){
        for (int i = completar.length(); i < 30; i++) {
            completar += " ";
        }
        return completar;
    }
    
    
    
    /**
     * Verifica se na posicao hash já havia um arquivo anteriormente, se ja tinha, grava sem sobrescrever o 'próximo'
     * @param posicaoProximoCodigo
     * @param posicaoInicialArquivo
     * @throws IOException 
     */
    public static void verificaOndeGravar(int posicaoProximoCodigo, int posicaoInicialArquivo) throws IOException{
            arquivo.seek(posicaoProximoCodigo);
            int proximoCodigo = arquivo.readInt();
            arquivo.seek(posicaoInicialArquivo);
            if (proximoCodigo == -1) { // nao existia nada
                gravaNovoArquivoComProximo();
            }else{
                gravaNovoArquivoSemProximo();
            }
    }
    
    /**
     * Verifica onde tem uma posição 'vazia' para gravar
     * @param posicaoAtual
     * @throws IOException 
     */
    public static void gravarPosicoesProximas(int posicaoAtual) throws IOException{
        if (posicaoAtual == arquivo.length()) {
            arquivo.seek(posicaoAtual);
            gravaNovoArquivoComProximo();
        }else{
            arquivo.seek(posicaoAtual);
            int posicaoProximo = arquivo.readInt();
            arquivo.seek(posicaoAtual+132);
            int proximaPosicao = arquivo.readInt();
            //verifica se a posição atual tem ou não registro, se não tiver, verificar se ja tinha registro anteriormente
            if(posicaoProximo == -1){
                arquivo.seek(posicaoAtual);
                if(proximaPosicao == -1){
                    gravaNovoArquivoComProximo();
                }else{
                    gravaNovoArquivoSemProximo();
                }
                
            }else{
                if(proximaPosicao == -1){
                    arquivo.seek(posicaoAtual+132);
                    arquivo.writeInt((int)arquivo.length());
                    gravarPosicoesProximas((int)arquivo.length());
                }else{
                    gravarPosicoesProximas(proximaPosicao);
                }
            }
            
        }
    }
    
    /**
     * Grava um novo arquivo em uma posição que não havia um usuario salvo anteriormente, e que consequentemente possui um próximo (fica no meio)
     * @throws IOException 
     */
    public static void gravaNovoArquivoComProximo() throws IOException{
        arquivo.writeInt(usuario.getCodigo());
        arquivo.writeChars(usuario.getNome());
        arquivo.writeChars(usuario.getQualificacao());
        arquivo.writeDouble(usuario.getSalario());
        arquivo.writeInt(usuario.getProximo());
        
    }
    /**
     * Grava um novo arquivo em uma posição que havia um usuário salvo anteriormente, porém ele era o último (não havia próximo)
     * @throws IOException 
     */
    public static void gravaNovoArquivoSemProximo() throws IOException{
        arquivo.writeInt(usuario.getCodigo());
        arquivo.writeChars(usuario.getNome());
        arquivo.writeChars(usuario.getQualificacao());
        arquivo.writeDouble(usuario.getSalario());
        
    }
    
    /**
     * Menu 
     * @return 
     */
    public static int procurarPor(){
        int menu = Integer.parseInt(JOptionPane.showInputDialog("Procurar por:\n"
                    + "1 - código\n"
                    + "2 - nome\n"
                    + "0 - voltar ao menu principal"));
        return menu;
    }
    
    /**
     * Remove o usuário de acordo com desejado (Código ou nome)
     * @throws IOException 
     */
    public static void removerUsuario() throws IOException{
        int menu;
        do{
            menu = procurarPor();
            switch(menu){
                case 1:
                    removerCodigo();           
                    break;
                case 2:
                    removerPorNome();
                    break;
            }
            
        }while(menu !=0);
    }
    
    /**
     * Remove por código
     * @throws IOException 
     */
    public static void removerCodigo() throws IOException{
        int codigo = Integer.parseInt(JOptionPane.showInputDialog("Informe o código para remoção"));
        if(!verificarCadastrado(codigo)){
            JOptionPane.showMessageDialog(null, "Código não cadastrado");
        }else{
            int posicao = hashInicialCodigo(codigo);
            arquivo.seek(posicao);
            int codigoCadastrado = arquivo.readInt();
            if (codigoCadastrado == codigo) {
                arquivo.seek(posicao);
                arquivo.writeInt(-1);
                arquivo.writeChars(String.format("%1$30s", ""));
                arquivo.writeChars(String.format("%1$30s", ""));
                arquivo.writeDouble(0);
                // tem ou não próximo -- escrever -1 ou deixar como 
            }else{
                removerCodigoProximos(posicao+132, codigo);
            }
        }
    }
    
    /**
     * Procura próximos para remover
     * @param proxPosicao
     * @param codigoProcurado
     * @throws IOException 
     */
    public static void removerCodigoProximos(int proxPosicao, int codigoProcurado) throws IOException{
        arquivo.seek(proxPosicao);
        int posicaoProximo = arquivo.readInt();
        if(posicaoProximo != -1){
            arquivo.seek(posicaoProximo);
            int codicoProximo = arquivo.readInt();
            if (codicoProximo == codigoProcurado) {
                arquivo.seek(posicaoProximo);
                arquivo.writeInt(-1);
                arquivo.writeChars(String.format("%1$30s", ""));
                arquivo.writeChars(String.format("%1$30s", ""));
                arquivo.writeDouble(0);
                JOptionPane.showMessageDialog(null, "Removido com sucesso!");
            }else{
                removerCodigoProximos(posicaoProximo+132, codigoProcurado);
            }
        }
    }
    
    /**
     * Método que remove o usuário por nome
     * @throws IOException 
     */
    public static void removerPorNome() throws IOException{
        arquivo.seek(0);
        String nome;
        boolean certo = false;
        do{
            nome = JOptionPane.showInputDialog("Informe o nome do usuário");
            if(nome.length()<31 ){
                certo = true;
                if(nome.length() <30){
                    for (int i = nome.length(); i < 30; i++) {
                        nome += " ";
                    }
                    
                }
            }else{
                JOptionPane.showMessageDialog(null, "Nome informado muito grande!");
            }
        }while(!certo);
        procuraNome(0, nome, "");
    }
    
    /**
     * Procura o usuário por nome e ja remove
     * @param posicaoBusca
     * @param nomeBuscado
     * @throws IOException 
     */
    public static void procuraNome(int posicaoBusca, String nomeBuscado, String msg) throws IOException{
        if(posicaoBusca < arquivo.length()){
            arquivo.seek(posicaoBusca);
            int codigo = arquivo.readInt();
            if( codigo != -1){
                boolean achou = true;
                for (int i = 0; i < 30; i++) {
                    char a = arquivo.readChar();
                    if (a != nomeBuscado.charAt(i)) {
                        achou = false;
                        break;
                    }
                }
                int posicao = posicaoBusca + 136;
                if(achou){
                    arquivo.seek(posicaoBusca);
                    arquivo.writeInt(-1);
                    arquivo.writeChars(String.format("%1$30s", ""));
                    arquivo.writeChars(String.format("%1$30s", "")); 
                    arquivo.writeDouble(0);
                    msg = "Removido com sucesso!";
                }
                procuraNome(posicao, nomeBuscado, msg);
            }else{
                int posicao = (int) arquivo.getFilePointer();
                procuraNome(posicao+132, nomeBuscado, msg);
            }
        }else{
            if(msg.length() < 2){
                JOptionPane.showMessageDialog(null, "Não foi encontrado");
            }else{
                JOptionPane.showMessageDialog(null, msg);
            }
        }
    }
    
    /**
     * Mostra os dados do usuário buscado
     * @throws IOException 
     */
    public static void consultar() throws IOException{
        int menu;
        do{
            menu = procurarPor();
            switch(menu){
                case 1: // codigo
                    consultarPorCodigo(); 
                    break;
                case 2: // nome
                    consultarPorNome();
                    break;
            }
        }while(menu != 0);
    }
    
    /**
     * Busca os dados do usuário de acordo com o código desejado
     * @throws IOException 
     */
    public static void consultarPorCodigo() throws IOException{
        int codigo = Integer.parseInt(JOptionPane.showInputDialog("Digite o código para consultar"));
        if(verificarCadastrado(codigo)){
            int hash = hashInicialCodigo(codigo);
            int posicao = procuraCodigo(hash, codigo);
            mostraConsulta(posicao);
        }else{
            JOptionPane.showMessageDialog(null, "Código não cadastrado");
        }
    }
    
    /**
     * Mostra os dados do usuário de acordo com a posicao informada
     * @param posicao
     * @throws IOException 
     */
    public static void mostraConsulta(int posicao) throws IOException{
        arquivo.seek(posicao);
        String resultado = "Código - Nome: " + arquivo.readInt() + " - " ;
        String nome = "";
        String qualificacao = "";
        for (int i = 0; i < 30; i++) {
            nome += arquivo.readChar();
        }
       for (int i = 0; i < 30; i++) {
            qualificacao += arquivo.readChar();
        }
       resultado += nome+"\n Qualificação: "+ qualificacao+"\n Salário: "+ arquivo.readDouble();
       JOptionPane.showMessageDialog(null, resultado);
        
    }
    
    /**
     * Consulta Por nome e ja mostra 
     * @throws IOException 
     */
    public static void consultarPorNome() throws IOException{
        String palavra = JOptionPane.showInputDialog("Digite o nome para consultar");
        int posicao = procuraPorNome(palavra, 0);
        if(posicao != -1){
            mostraConsulta(posicao);
        }else{
            JOptionPane.showMessageDialog(null, "Nome não encontrado");
        }
        
    }
    
    /**
     * Procura por nome e retorna a posição do registro no arquivo
     * @param nomeBuscado
     * @param posicaoBusca
     * @return
     * @throws IOException 
     */
    public static int procuraPorNome(String nomeBuscado, int posicaoBusca) throws IOException{
        int posicaoAchado = -1;
        if(posicaoBusca < arquivo.length()){
            arquivo.seek(posicaoBusca);
            boolean achou = false;
            if(arquivo.readInt() != -1){
                String nomeAchado="";
                for (int i = 0; i < 30; i++) {
                    char a = arquivo.readChar();
                    if(a != ' '){
                         nomeAchado += a;
                    }
                }
                achou = nomeBuscado.equals(nomeAchado);
            }
            
            if(achou){
                posicaoAchado = posicaoBusca;
                return posicaoAchado;
            }else{
                posicaoAchado = procuraPorNome(nomeBuscado, posicaoBusca+136);
            }
            
        }
        return posicaoAchado;
    }

    /**
     * Altera o usuário
     * @throws IOException 
     */
    public static void alterarUsuario() throws IOException{
        int codigo = Integer.parseInt(JOptionPane.showInputDialog("Informe o código que deseja alterar"));
        if(verificarCadastrado(codigo)){
            cadastraInfoUsuario(usuario, true);
            int posicaohash = hashInicialCodigo(codigo);
            int posicao = procuraCodigo(posicaohash, codigo);
            arquivo.seek(posicao);
            arquivo.writeInt(usuario.getCodigo());
            arquivo.writeChars(usuario.getNome());
            arquivo.writeChars(usuario.getQualificacao());
            arquivo.writeDouble(usuario.getSalario());
        }else{
            JOptionPane.showMessageDialog(null, "Código não cadastrado");
        }
    }
    
    /**
     * Procura por código e retorna o a posição do registro no arquivo
     * @param posicaoProcura
     * @param codigoProcurado
     * @return
     * @throws IOException 
     */
    public static int procuraCodigo(int posicaoProcura, int codigoProcurado) throws IOException{
        arquivo.seek(posicaoProcura);
        int codigoAchado = arquivo.readInt();
        if(codigoAchado == codigoProcurado){
            return posicaoProcura;
        }else{
            arquivo.seek(posicaoProcura+132);
            int proximaPosicaoProcura = arquivo.readInt();
                posicaoProcura = procuraCodigo(proximaPosicaoProcura, codigoProcurado);
            
        }
        return posicaoProcura;
    }
    
    /**
     * Mostra todos os registros já cadastrados
     * @param posicao
     * @param mostrar
     * @throws IOException 
     */
    public static void mostrarTudo(int posicao, String mostrar) throws IOException{
        if(posicao < arquivo.length()){
            arquivo.seek(posicao);
            int ponteiro = (int) arquivo.getFilePointer();
            int codigo = arquivo.readInt();
            if(codigo != -1){
                String nome = "";
                String qualificacao = "";
                if(codigo != -1){
                    for (int i = 0; i < 30; i++) {
                        char a = arquivo.readChar();
                        if( a != ' '){
                            nome += a;
                        }
                    }
                    for (int i = 0; i < 30; i++) {
                        char a = arquivo.readChar();
                        if( a != ' '){
                            qualificacao += a;
                        }
                    }
                    double salario = arquivo.readDouble();
                    int proximo = arquivo.readInt();
                    mostrar += "Inicio do Arquivo: "+ ponteiro+"\n"+
                                "Código: " +codigo +"\n"+
                                "Nome: "+ nome+"\n"+
                                "Qualificação: " + qualificacao + "\n"+
                                "Salário: "+ salario+"\n"+
                                "Aponta para a posição do Arquivo: "+proximo+"\n\n";
                    
                }
            }
            mostrarTudo(posicao+136, mostrar);
        }
        else{
            JOptionPane.showMessageDialog(null, mostrar+" Tamanho Total do Arquivo: "+ arquivo.length());
        }
    }
    
    /**
     * Mostra todos os registros por hash
     * @param posicao -- posicao no arquivo
     * @param mostrar -- string concatenada
     * @return String
     * @throws IOException 
     */
    public static String mostrarPorHash(int posicao,String mostrar) throws IOException{
        int proximo = -1;
        arquivo.seek(posicao);
        int codigo = arquivo.readInt();
        if(codigo != -1){
            mostrar += "\n Código: "+codigo;
            String nome = "";
            String qualificacao = "";
            if(codigo != -1){
                for (int i = 0; i < 30; i++) {
                    char a = arquivo.readChar();
                    if( a != ' '){
                        nome += a;
                    }
                }
                for (int i = 0; i < 30; i++) {
                    char a = arquivo.readChar();
                    if( a != ' '){
                        qualificacao += a;
                    }
                }
                mostrar += "\nNome: "+ nome+"\nQualificação: "+ qualificacao+"\nSalário: "+arquivo.readDouble()+"\n\n";
                proximo = arquivo.readInt();
            }
          
        }
        if(proximo == -1){
            return mostrar;
        }else{
            return mostrarPorHash(proximo, mostrar);
        }
    }
}
