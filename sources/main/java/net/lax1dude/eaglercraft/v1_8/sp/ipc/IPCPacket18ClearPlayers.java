package net.lax1dude.eaglercraft.v1_8.sp.ipc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Copyright (c) 2023-2024 lax1dude. All Rights Reserved.
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
public class IPCPacket18ClearPlayers implements IPCPacketBase {
	
	public static final int ID = 0x18;
	
	public String worldName = null;
	
	public IPCPacket18ClearPlayers(String worldName) {
		this.worldName = worldName;
	}
	
	public IPCPacket18ClearPlayers() {
	}

	@Override
	public void deserialize(DataInput bin) throws IOException {
		worldName = bin.readUTF();
	}

	@Override
	public void serialize(DataOutput bin) throws IOException {
		bin.writeUTF(worldName);
	}

	@Override
	public int id() {
		return ID;
	}

	@Override
	public int size() {
		return IPCPacketBase.strLen(worldName);
	}

}
