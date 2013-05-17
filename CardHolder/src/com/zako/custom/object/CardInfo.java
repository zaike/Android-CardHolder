package com.zako.custom.object;

import java.io.Serializable;

public class CardInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	String cardId;
	String cardName;
	String cardMemo;
	byte[] cardFrontImage;
	byte[] cardBackImage;
	String cardModDate;

	public CardInfo() {
		this.cardId = "";
		this.cardName = "";
		this.cardFrontImage = null;
		this.cardBackImage = null;
		this.cardMemo = "";
		this.cardModDate = "";
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	public String getCardMemo() {
		return cardMemo;
	}

	public void setCardMemo(String cardMemo) {
		this.cardMemo = cardMemo;
	}

	public byte[] getCardFrontImage() {
		return cardFrontImage;
	}

	public void setCardFrontImage(byte[] cardFrontImage) {
		this.cardFrontImage = cardFrontImage;
	}

	public byte[] getCardBackImage() {
		return cardBackImage;
	}

	public void setCardBackImage(byte[] cardBackImage) {
		this.cardBackImage = cardBackImage;
	}

	public String getCardModDate() {
		return cardModDate;
	}

	public void setCardModDate(String cardModDate) {
		this.cardModDate = cardModDate;
	}
}
