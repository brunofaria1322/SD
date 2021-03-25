import java.text.SimpleDateFormat;

public class User {
    private String password;
    private String nome;
    private String departamento;
    private String telefone;
    private String morada;
    private String numcc;
    private SimpleDateFormat valcc;
    public User(String password, String nome, String departamento, String telefone, String morada, String numcc, SimpleDateFormat valcc){
        this.password = password;
        this.nome = nome;
        this.departamento = departamento;
        this.telefone = telefone;
        this.morada = morada;
        this.numcc = numcc;
        this.valcc = valcc;
    }

}
