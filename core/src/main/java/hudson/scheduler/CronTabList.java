/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.scheduler;

import antlr.ANTLRException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Vector;

/**
 * {@link CronTab} list (logically OR-ed).
 *
 * @author Kohsuke Kawaguchi
 */
public final class CronTabList {
    private final Vector<CronTab> tabs;

    public CronTabList(Collection<CronTab> tabs) {
        this.tabs = new Vector<CronTab>(tabs);
    }

    /**
     * Returns true if the given calendar matches
     */
    public synchronized boolean check(Calendar cal) {
        for (CronTab tab : tabs) {
            if(tab.check(cal))
                return true;
        }
        return false;
    }

    /**
     * Checks if this crontab entry looks reasonable,
     * and if not, return an warning message.
     *
     * <p>
     * The point of this method is to catch syntactically correct
     * but semantically suspicious combinations, like
     * "* 0 * * *"
     */
    public String checkSanity() {
        for (CronTab tab : tabs) {
            String s = tab.checkSanity();
            if(s!=null)     return s;
        }
        return null;
    }

    public static CronTabList create(String format) throws ANTLRException {
        return create(format,null);
    }

    public static CronTabList create(String format, Hash hash) throws ANTLRException {
        Vector<CronTab> r = new Vector<CronTab>();
        int lineNumber = 0;
        for (String line : format.split("\\r?\\n")) {
            lineNumber++;
            line = line.trim();
            if(line.length()==0 || line.startsWith("#"))
                continue;   // ignorable line
            try {
                r.add(new CronTab(line,lineNumber,hash));
            } catch (ANTLRException e) {
                throw new ANTLRException(Messages.CronTabList_InvalidInput(line,e.toString()),e);
            }
        }
        return new CronTabList(r);
    }
}
