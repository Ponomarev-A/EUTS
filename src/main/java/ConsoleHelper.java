import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Ponomarev on 13.11.2015.
 */
public class ConsoleHelper
{
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message)
    {
        System.out.print(message);
    }

    public static void writeMessageLn(String message)
    {
        System.out.println(message);
    }

    public static String readString()
    {
        String s = null;
        try {
            while ((s = bufferedReader.readLine()) == null);
        } catch (IOException e) {
            writeMessageLn("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
        }

        return s;
    }

    public static int readInt()
    {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                writeMessageLn("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
    }
}
