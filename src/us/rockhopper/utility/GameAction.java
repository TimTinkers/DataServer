package us.rockhopper.utility;

public enum GameAction {
	GRAB_ONE(0), GRAB_TWO(1), GRAB_THREE(2), GRAB_FOUR(3), GRAB_FIVE(4), TRANSFORM_HELD(5), THROW_FRIEND(
			6), THROW_FOE_ONE(7), THROW_FOE_TWO(8), INVALID(-1);

	private final int botCode;

	private GameAction(int botCode) {
		this.botCode = botCode;
	}

	public int getBotCode() {
		return botCode;
	}
}
