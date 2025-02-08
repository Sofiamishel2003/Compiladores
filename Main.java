import clases.RegexConverter;

public class Main {
    public static void main(String[] args) {
        String regex1 = "a|b*";
        String regex2 = "(a|b)*c";
        String regex3 = "a(b|c)/*d";
        String regex4 = "[abc]+";
        String regex5 = "\"string\"*#a|b";
        String regex6 = "a(b|c)#(d*)";

        System.out.println("Expresión infija: " + regex1);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex1));

        System.out.println("\nExpresión infija: " + regex2);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex2));

        System.out.println("\nExpresión infija: " + regex3);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex3));

        System.out.println("\nExpresión infija: " + regex4);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex4));

        System.out.println("\nExpresión infija: " + regex5);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex5));

        System.out.println("\nExpresión infija: " + regex6);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex6));
    }
}
