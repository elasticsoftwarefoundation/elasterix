package org.elasticsoftware.sip;

import org.jboss.netty.channel.*;

/**
 * @author Leonard Wolters
 */
public class DummyChannelHandlerContext implements ChannelHandlerContext {

    @Override
    public boolean canHandleDownstream() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canHandleUpstream() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object getAttachment() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Channel getChannel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandler getHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChannelPipeline getPipeline() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendDownstream(ChannelEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendUpstream(ChannelEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAttachment(Object arg0) {
        // TODO Auto-generated method stub

    }

}
