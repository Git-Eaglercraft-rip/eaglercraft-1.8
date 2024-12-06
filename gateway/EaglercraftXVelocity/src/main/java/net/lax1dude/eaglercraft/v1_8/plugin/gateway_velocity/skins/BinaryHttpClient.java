package net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.skins;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.net.ssl.SSLEngine;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.proxy.network.TransportType.Type;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.EaglerXVelocity;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.EaglerXVelocityVersion;

/**
 * Copyright (c) 2022-2024 lax1dude, ayunami2000. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
public class BinaryHttpClient {

	public static class Response {

		public final int code;
		public final byte[] data;
		public final Throwable exception;

		public Response(int code, byte[] data) {
			this.code = code;
			this.data = data;
			this.exception = null;
		}

		public Response(Throwable exception) {
			this.code = -1;
			this.data = null;
			this.exception = exception;
		}

	}

	private static class NettyHttpChannelFutureListener implements ChannelFutureListener {

		protected final String method;
		protected final URI requestURI;
		protected final Consumer<Response> responseCallback;

		protected NettyHttpChannelFutureListener(String method, URI requestURI, Consumer<Response> responseCallback) {
			this.method = method;
			this.requestURI = requestURI;
			this.responseCallback = responseCallback;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				String path = requestURI.getRawPath()
						+ ((requestURI.getRawQuery() == null) ? "" : ("?" + requestURI.getRawQuery()));
				HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
						HttpMethod.valueOf(method), path);
				request.headers().set(HttpHeaderNames.HOST, (Object) requestURI.getHost());
				request.headers().set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 " + EaglerXVelocityVersion.ID + "/" + EaglerXVelocityVersion.VERSION);
				future.channel().writeAndFlush(request);
			} else {
				addressCache.invalidate(requestURI.getHost());
				responseCallback.accept(new Response(new IOException("Connection failed")));
			}
		}

	}

	private static class NettyHttpChannelInitializer extends ChannelInitializer<Channel> {

		protected final Consumer<Response> responseCallback;
		protected final boolean ssl;
		protected final String host;
		protected final int port;

		protected NettyHttpChannelInitializer(Consumer<Response> responseCallback, boolean ssl, String host, int port) {
			this.responseCallback = responseCallback;
			this.ssl = ssl;
			this.host = host;
			this.port = port;
		}

		@Override
		protected void initChannel(Channel ch) throws Exception {
			ch.pipeline().addLast("timeout", new ReadTimeoutHandler(5L, TimeUnit.SECONDS));
			if (this.ssl) {
				SSLEngine engine = SslContextBuilder.forClient().build().newEngine(ch.alloc(), host, port);
				ch.pipeline().addLast("ssl", new SslHandler(engine));
			}

			ch.pipeline().addLast("http", new HttpClientCodec());
			ch.pipeline().addLast("handler", new NettyHttpResponseHandler(responseCallback));
		}

	}
	
	private static class NettyHttpResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

		protected final Consumer<Response> responseCallback;
		protected int responseCode = -1;
		protected ByteBuf buffer = null;

		protected NettyHttpResponseHandler(Consumer<Response> responseCallback) {
			this.responseCallback = responseCallback;
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
			if (msg instanceof HttpResponse) {
				HttpResponse response = (HttpResponse) msg;
				responseCode = response.status().code();
				if (responseCode == HttpResponseStatus.NO_CONTENT.code()) {
					this.done(ctx);
					return;
				}
			}
			if (msg instanceof HttpContent) {
				HttpContent content = (HttpContent) msg;
				if(buffer == null) {
					buffer = ctx.alloc().buffer();
				}
				this.buffer.writeBytes(content.content());
				if (msg instanceof LastHttpContent) {
					this.done(ctx);
				}
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			responseCallback.accept(new Response(cause));
		}

		private void done(ChannelHandlerContext ctx) {
			try {
				byte[] array;
				if(buffer != null) {
					array = new byte[buffer.readableBytes()];
					buffer.readBytes(array);
					buffer.release();
				}else {
					array = new byte[0];
				}
				responseCallback.accept(new Response(responseCode, array));
			}finally {
				ctx.channel().pipeline().remove(this);
				ctx.channel().close();
			}
		}

	}

	private static final Cache<String, InetAddress> addressCache = CacheBuilder.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).build();
	private static EventLoopGroup eventLoop = null;

	public static void asyncRequest(String method, URI uri, Consumer<Response> responseCallback) {
		EventLoopGroup eventLoop = getEventLoopGroup();
		
		int port = uri.getPort();
		boolean ssl = false;
		String scheme = uri.getScheme();
		switch(scheme) {
		case "http":
			if(port == -1) {
				port = 80;
			}
			break;
		case "https":
			if(port == -1) {
				port = 443;
			}
			ssl = true;
			break;
		default:
			responseCallback.accept(new Response(new UnsupportedOperationException("Unsupported scheme: " + scheme)));
			return;
		}
		
		String host = uri.getHost();
		InetAddress inetHost = addressCache.getIfPresent(host);
		if (inetHost == null) {
			try {
				inetHost = InetAddress.getByName(host);
			} catch (UnknownHostException ex) {
				responseCallback.accept(new Response(ex));
				return;
			}
			addressCache.put(host, inetHost);
		}
		
		(new Bootstrap()).channelFactory(EaglerXVelocity.getEagler().getChannelFactory()).group(eventLoop)
				.handler(new NettyHttpChannelInitializer(responseCallback, ssl, host, port))
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000).option(ChannelOption.TCP_NODELAY, true)
				.remoteAddress(inetHost, port).connect()
				.addListener(new NettyHttpChannelFutureListener(method, uri, responseCallback));
	}

	private static EventLoopGroup getEventLoopGroup() {
		if(eventLoop == null) {
			eventLoop = EaglerXVelocity.getEagler().getTransportType().createEventLoopGroup(Type.WORKER);
		}
		return eventLoop;
	}

	public static void killEventLoop() {
		if(eventLoop != null) {
			EaglerXVelocity.logger().info("Stopping skin cache HTTP client...");
			eventLoop.shutdownGracefully();
			try {
				eventLoop.awaitTermination(30l, TimeUnit.SECONDS);
			} catch (InterruptedException var13) {
				;
			}
			eventLoop = null;
		}
	}

}
