import java.util.*;

public class Main {
    public static void main(String[] args) {
        String str="аркtический";
        String str1="арктический";
        String str2="https://PlayBack.ru/catalog/1652.html";
        String str3="https://PlayBack.ru/";
        String str4="https://PlayBack.ru/basket.html";
        String str5="https://PlayBack.ru/product/1125196.html";
        String str6="https://PlayBack.ru/product/1125198.html";
        /*try {
            Document doc = Jsoup.connect(str6).get();
            Lem lem=new Lem(doc.html());
            System.out.println(lem.getMapLemmas());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Integer[] ints={4,5,1,2,6};
        List<Integer> integerList=new ArrayList<>(Arrays.asList(ints));
        System.out.println(integerList);
        Collections.sort(integerList,(O1,O2)->O1.compareTo(O2));
        System.out.println(integerList);
        //System.out.println(str1.matches("[А-Яа-яЁё&&[^A-Za-z]]+"));
    }
}
