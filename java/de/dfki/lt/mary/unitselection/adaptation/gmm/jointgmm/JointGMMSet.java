/**
 * Copyright 2007 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * Permission is hereby granted, free of charge, to use and distribute
 * this software and its documentation without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of this work, and to
 * permit persons to whom this work is furnished to do so, subject to
 * the following conditions:
 * 
 * 1. The code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 * 2. Any modifications must be clearly marked as such.
 * 3. Original authors' names are not deleted.
 * 4. The authors' names are not used to endorse or promote products
 *    derived from this software without specific prior written
 *    permission.
 *
 * DFKI GMBH AND THE CONTRIBUTORS TO THIS WORK DISCLAIM ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL DFKI GMBH NOR THE
 * CONTRIBUTORS BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 * PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS
 * ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */

package de.dfki.lt.mary.unitselection.adaptation.gmm.jointgmm;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.dfki.lt.machinelearning.ContextualGMMParams;
import de.dfki.lt.machinelearning.GMM;
import de.dfki.lt.mary.unitselection.adaptation.VocalTractTransformationData;
import de.dfki.lt.signalproc.analysis.LsfFileHeader;
import de.dfki.lt.signalproc.util.MaryRandomAccessFile;

/**
 * @author oytun.turk
 *
 * A collection of JointGMMs, 
 * i.e. joint source-target gmms each trained separately using groups of source-target feature vectors
 * 
 */
public class JointGMMSet extends VocalTractTransformationData {
    public static final String DEFAULT_EXTENSION = ".jgs";
    public JointGMM[] gmms;
    public ContextualGMMParams cgParams;
    
    public JointGMMSet()
    {
        allocate(0, null);
    }
    
    public JointGMMSet(JointGMMSet existing)
    {
        gmms = null;
        if (existing!=null)
        {
            allocate(existing.gmms.length, null);
            for (int i=0; i<existing.gmms.length; i++)
                gmms[i] = new JointGMM(existing.gmms[i]);
            
            cgParams = new ContextualGMMParams(existing.cgParams);
        }
    }
    
    public JointGMMSet(int numGMMs)
    {
        this(numGMMs, null);
    }
    
    public JointGMMSet(int numGMMs, ContextualGMMParams cgParamsIn)
    {        
        allocate(numGMMs, cgParamsIn);
    }
    
    public void allocate(int numGMMs, ContextualGMMParams cgParamsIn)
    {
        if (numGMMs>0)
            gmms = new JointGMM[numGMMs];
        else
            gmms = null;

        if (cgParamsIn!=null)
            cgParams = new ContextualGMMParams(cgParamsIn);
        else if (numGMMs>0)
            cgParams = new ContextualGMMParams(numGMMs);
        else
            cgParams = null;
    }
    
    public JointGMMSet(String jointGMMFile)
    {
        read(jointGMMFile);
    }
    
    public void write(String jointGMMFile)
    {
        MaryRandomAccessFile stream = null;
        try {
            stream = new MaryRandomAccessFile(jointGMMFile, "rw");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (stream!=null)
        {
            if (gmms!=null && gmms.length>0)
            {
                try {
                    stream.writeInt(gmms.length);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                
                if (cgParams!=null)
                    cgParams.write(stream);
                
                for (int i=0; i<gmms.length; i++)
                {
                    if (gmms[i]!=null)
                    {
                        try {
                            stream.writeBoolean(true);  //gmm is not null
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        
                        gmms[i].write(stream);
                    }
                    else
                    {
                        try {
                            stream.writeBoolean(false); //gmm is null
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } 
                    }
                }
            }
            else
            {
                try {
                    stream.writeInt(0);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            try {
                stream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void read(String jointGMMFile)
    {
        MaryRandomAccessFile stream = null;
        try {
            stream = new MaryRandomAccessFile(jointGMMFile, "r");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (stream!=null)
        {
            int numGMMs = 0;
            try {
                numGMMs = stream.readInt();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            if (numGMMs>0)
            {
                ContextualGMMParams tmpCgParams = new ContextualGMMParams();
                tmpCgParams.read(stream);
                
                allocate(numGMMs, tmpCgParams);
                
                for (int i=0; i<numGMMs; i++)
                {
                    boolean isGmmExisting = false;
                    
                    try {
                        isGmmExisting = stream.readBoolean();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    if (isGmmExisting)
                        gmms[i] = new JointGMM(stream);
                }
            }
            
            try {
                stream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
