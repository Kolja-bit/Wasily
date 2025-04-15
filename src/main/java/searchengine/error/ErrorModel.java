package searchengine.error;

import lombok.Data;

@Data
public class ErrorModel {
    private int StatusCode ;
    public String getReturnResponse(){
        String str= "";
        char firstChar=String.valueOf(getStatusCode()).charAt(0);
        if (firstChar=='5'){
            str="Ошибка сервера";
        }
        if (firstChar=='4'){
            str="Запрошенный ресупс не найден";
        }
        return str;
    }
}
