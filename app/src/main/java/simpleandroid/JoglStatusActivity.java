package simpleandroid;

import android.os.Bundle;

import com.ingenieur.andyelderscrolls.utils.SopInterceptor;

import java.io.PrintStream;

import jogamp.newt.driver.android.NewtVersionActivity;

//j o g a m p . n e w t . d r i v e r . a n d r o i d . N e w t V e r s i o n A c t i v i t y L a u n c h e r
public class JoglStatusActivity extends NewtVersionActivity
{
    public void onCreate(final Bundle state) {

		//make sure teh System.out calls get onto teh log
		PrintStream interceptor = new SopInterceptor(System.out, "sysout");
		System.setOut(interceptor);
		PrintStream interceptor2 = new SopInterceptor(System.err, "syserr");
		System.setErr(interceptor2);
        super.onCreate(state);
    }
}
