package de.henry4k.rpcraft;

class PlayerSettings {
	/**
	 * Chat mode that is used when the player talks without using
	 * a specific chat command.
	 */
	private ChatMode chatMode;
	private int radioChannel;

	public PlayerSettings() {
		chatMode = ChatMode.TALK;
		radioChannel = -1;
	}

	public ChatMode getChatMode() {
		return chatMode;
	}

	public void setChatMode( ChatMode mode ) {
		chatMode = mode;
	}

	public int getRadioChannel() {
		return radioChannel;
	}

	public void setRadioChannel( int channel ) {
		radioChannel = channel;
	}
}
