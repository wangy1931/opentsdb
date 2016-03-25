package net.opentsdb.tsd;


import net.opentsdb.core.TSDB;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.ssl.SslContext;
import org.jboss.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

import static org.jboss.netty.channel.Channels.pipeline;

public class SSLPipelineFactory extends PipelineFactory {
    private final SslContext sslContext;
    public SSLPipelineFactory(final TSDB tsdb, final RpcManager manager) throws CertificateException, SSLException {
        super(tsdb, manager);
        final SelfSignedCertificate ssc = new SelfSignedCertificate();
        this.sslContext = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        final ChannelPipeline pipeline = pipeline();
        pipeline.addLast("ssl", this.sslContext.newHandler());
        addTSDBHandler(pipeline);
        return pipeline;
    }
}
