package commands;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;

//this class redirects output to TextArea
class TextAreaOutputStream extends OutputStream {

    JTextArea ta;

    public TextAreaOutputStream(JTextArea _ta) {   
        ta = _ta;
    }

    
    @Override
    public void write(int b) throws IOException {
             
       ta.append(String.valueOf((char)b));
       
    }
     
}
