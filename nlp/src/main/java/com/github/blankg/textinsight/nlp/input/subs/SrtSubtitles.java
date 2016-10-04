package com.github.blankg.textinsight.nlp.input.subs;

import com.github.blankg.textinsight.nlp.NLPManager;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SrtSubtitles {

	private long timescale;
	private Date creationTime;

	List<Line> subs = new LinkedList<Line>();

	public List<Line> getSubs() {
		return subs;
	}

	public SrtSubtitles() {
		timescale = 1000;// Text tracks use millieseconds
		creationTime = new Date();
	}


	public static class Line {
		private long from;
		private long to;
		private String text;
		private int sentiment;
		private List<NLPManager.NLPData> nlpData;

		public Line(long from, long to, String text) {
			this.from = from;
			this.to = to;
			this.setText(text);
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public long getFrom() {
			return from;
		}

		public void setFrom(long from) {
			this.from = from;
		}

		public long getTo() {
			return to;
		}

		public void setTo(long to) {
			this.to = to;
		}

		public List<NLPManager.NLPData> getNLPData() {
			return nlpData;
		}

		public void setNLPData(List<NLPManager.NLPData> nlpData) {
			this.nlpData = nlpData;
		}

		public int getSentiment() {
			return sentiment;
		}

		public void setSentiment(int sentiment) {
			this.sentiment = sentiment;
		}
	}
}
