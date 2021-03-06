package net.floodlightcontroller.statistics;

import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.python.antlr.ast.boolopType;
import org.python.core.imp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostEntry {
	private static final Logger log = LoggerFactory.getLogger(HostEntry.class);
	private IPv4Address ip;
	
	public int importance;
	public int pi;
	public int piCopy;
	public double score;
	public boolean FIRST_SCORE;
	private int highFlowNumber;
	private int number; // Ni
	
	public HostEntry(IPv4Address ipAddress) {
		number = 0;
		highFlowNumber = 0;
		ip = ipAddress;
		pi = 0;
		importance = 1;
		score = 0;
		piCopy = 0;
		FIRST_SCORE = true;
	}
	
	public void init() {
		number = 0;
		highFlowNumber = 0;
		importance = 1;
		score = 0;
		pi = piCopy;
		piCopy = 0;
	}
	
	public void setScore(double s) {
		score = s;
	}
	public String toString() {
		return "highCount(" + String.valueOf(highFlowNumber) + "),"
				+ " count(" + String.valueOf(number) + "),"
				+ " pi(" + String.valueOf(pi) + "),"
				+ " score(" + String.valueOf((int)score) + "),"
				+ " importance(" + String.valueOf(importance) + ").";
	}
	public int getHighFlowNumber() {
		return highFlowNumber;
	}
	public int getNumber() {
		return number;
	}
	public double getScore() {
		return score;
	}
	
	public void compute() {
		// count part
		double temp = 0.0;
		if (number == 0) temp = 0.000001;
		else
			temp = (double)highFlowNumber / number;
		temp *= 10;
		int countScore = 0;
		for (int i : StatisticsCollector.scoreImptList) {
			if (i > temp) break;
			countScore += 1;
		}
		// pi part
		int piScore = 0;
		if (pi >= FTGuardManager.piPara1)
			piScore = 0;
		else if (pi >= FTGuardManager.piPara2)
			piScore = 1;
		else
			piScore = 2;
		double tempScore = countScore + piScore;
		if (FIRST_SCORE) {
			FIRST_SCORE = false;
			score = tempScore;
		}
		else
			score = FTGuardManager.alpha * tempScore + (1.0 - FTGuardManager.alpha) * score;

		if (score < 1.0) importance = 1;
		else if (score < 3.5) importance = 2;
		else importance = 3;
	}
	
	public void udpateByReply(OFFlowStatsEntry pse) {
		int packetCount = (int)pse.getPacketCount().getValue();
        number += 1;
        if (packetCount > FTGuardManager.highCount) {
            this.highFlowNumber += 1;
        }
	}
	
	public void addEntry(Match match) {
		number += 1;
	}
}
