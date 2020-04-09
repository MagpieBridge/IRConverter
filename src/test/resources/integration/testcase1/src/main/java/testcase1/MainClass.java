package testcase1;

import java.io.FileNotFoundException;

import de.upb.swt.mylib.ViewPrinter;

public class MainClass {

	public static void main(String... args) throws FileNotFoundException
	{
		MyView view=new MyView("Hi");
		ViewPrinter printer=new ViewPrinter(view);
		printer.printIt();
	}
}
