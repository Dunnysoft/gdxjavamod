/*
 * Created on Jun 28, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.dunnysoft.jflac;

import java.util.HashSet;
import java.util.Iterator;

import com.dunnysoft.jflac.metadata.StreamInfo;
import com.dunnysoft.jflac.util.ByteData;


/**
 * Class to handle PCM processors.
 * @author kc7bfi
 */
class PCMProcessors implements PCMProcessor {
    private HashSet<PCMProcessor> pcmProcessors = new HashSet<PCMProcessor>();
    
    /**
     * Add a PCM processor.
     * @param processor  The processor listener to add
     */
    public void addPCMProcessor(PCMProcessor processor) {
        synchronized (pcmProcessors) {
            pcmProcessors.add(processor);
        }
    }
     
    /**
     * Remove a PCM processor.
     * @param processor  The processor listener to remove
     */
    public void removePCMProcessor(PCMProcessor processor) {
        synchronized (pcmProcessors) {
            pcmProcessors.remove(processor);
        }
    }
    
    /**
     * Process the StreamInfo block.
     * @param info the StreamInfo block
     * @see com.dunnysoft.jflac.PCMProcessor#processStreamInfo(com.dunnysoft.jflac.metadata.StreamInfo)
     */
    public void processStreamInfo(StreamInfo info) {
        synchronized (pcmProcessors) {
            Iterator<PCMProcessor> it = pcmProcessors.iterator();
            while (it.hasNext()) {
                PCMProcessor processor = (PCMProcessor)it.next();
                processor.processStreamInfo(info);
            }
        }
    }
    
    /**
     * Process the decoded PCM bytes.
     * @param pcm The decoded PCM data
     * @see com.dunnysoft.jflac.PCMProcessor#processPCM(com.dunnysoft.jflac.util.ByteSpace)
     */
    public void processPCM(ByteData pcm) {
        synchronized (pcmProcessors) {
            Iterator<PCMProcessor> it = pcmProcessors.iterator();
            while (it.hasNext()) {
                PCMProcessor processor = (PCMProcessor)it.next();
                processor.processPCM(pcm);
            }
        }
    }

}
