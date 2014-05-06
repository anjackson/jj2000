package gui;

import javax.swing.filechooser.*;
import javax.swing.*;
import java.io.File;

public class JJFileFilter extends FileFilter {

    /** File filter description */
    public String desc = null;

    /** Array of accepted extensions */
    public String[] ext = null;

    /** 
     * Construct a JJFileFilter with the specified description and the list of
     * supported extensions.
     *
     * @param desc File filter description
     * @param ext Supported extensions
     * */
    public JJFileFilter(String desc,String[] ext) {
        this.desc = desc;
        this.ext = ext;
    }

    /** 
     * Construct a JJFileFilter with the specified description and the
     * supported extension.
     *
     * @param desc File filter description
     * @param extension Supported extension
     * */
    public JJFileFilter(String desc,String extension) {
        this.desc = desc;
        this.ext = new String[1];
        ext[0] = extension;
    }
    
    /**
     * Get the name excluding the extension of a file.
     * */
    public static String getNameNoExt(File f) {
        String name = null;
        String s = f.getAbsolutePath();
        int i = s.lastIndexOf('.');
	
        if (i>0 &&  i<s.length()-1) {
            name = s.substring(0,i).toLowerCase();
        }
        return name;
    }

    /**
     * Get the extension of a file.
     * */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
	
        if (i>0 && i<s.length()-1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
    /**
     *  Accept all directories and all ppm, pgm, or pgx files.
     * */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension!=null) {
            for(int i=0; i<ext.length; i++) {
                if(extension.equalsIgnoreCase(ext[i])) return true;
            }
            return false;
        }
	
        return false;
    }

    /**
     *  The description of this filter.
     * */
    public String getDescription() {
        return desc;
    }
}
