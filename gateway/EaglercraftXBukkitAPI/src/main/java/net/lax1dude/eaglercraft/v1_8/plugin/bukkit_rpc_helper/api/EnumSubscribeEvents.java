package net.lax1dude.eaglercraft.v1_8.plugin.bukkit_rpc_helper.api;

import net.lax1dude.eaglercraft.v1_8.plugin.backend_rpc_protocol.pkt.client.CPacketRPCSubscribeEvents;

/**
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
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
public enum EnumSubscribeEvents {

	/** @see net.lax1dude.eaglercraft.v1_8.plugin.bukkit_rpc_helper.api.event.EventWebViewOpenClose */
	EVENT_WEBVIEW_OPEN_CLOSE(CPacketRPCSubscribeEvents.SUBSCRIBE_EVENT_WEBVIEW_OPEN_CLOSE),

	/** @see net.lax1dude.eaglercraft.v1_8.plugin.bukkit_rpc_helper.api.event.EventWebViewMessage */
	EVENT_WEBVIEW_MESSAGE(CPacketRPCSubscribeEvents.SUBSCRIBE_EVENT_WEBVIEW_MESSAGE),

	/** @see net.lax1dude.eaglercraft.v1_8.plugin.bukkit_rpc_helper.api.event.EventToggledVoice */
	EVENT_TOGGLE_VOICE(CPacketRPCSubscribeEvents.SUBSCRIBE_EVENT_TOGGLE_VOICE);

	protected static final EnumSubscribeEvents[] _VALUES = values();

	public final int bit;

	private EnumSubscribeEvents(int bit) {
		this.bit = bit;
	}

}
