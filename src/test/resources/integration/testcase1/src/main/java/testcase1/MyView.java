package testcase1;

import de.upb.swt.mylib.LibView;

public class MyView extends LibView {

	String myString = "Hello";

	public MyView(String str) {
		this.myString = str;
	}

	@Override
	public void print() {
		System.out.println(myString);
	}

}
