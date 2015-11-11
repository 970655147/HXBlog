package com.hx.util;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;


public class Test02PriorityTest {
	
	// ����PriorityQueue
	public static void main(String []args) {
		
		Queue<Entry> queue = new PriorityQueue<>();
//		for(int i=0; i<10; i++) {
		for(int i=10; i>0; i--) {
			queue.add(new Entry(i) );
		}
		Iterator<Entry> it = queue.iterator();
		while(it.hasNext()) {
			Log.log(it.next().toString() );
		}
		Log.horizon();
		
		// ���������compareTo[Ҳ������CommentEntry�ıȽϷ�ʽ], �Ѷ�����СԪ��
		Log.log(queue.poll().toString() );
		
	}
	
	// Entry
	static class Entry implements Comparable<Entry> {
		// id
		public Integer id;

		// ��ʼ��
		public Entry() {
			super();
		}
		public Entry(Integer id) {
			super();
			this.id = id;
		}

		// for PriorityQueue
		@Override
		public int compareTo(Entry other) {
			return id - other.id;
		}
		
		// for debug ..
		public String toString() {
			return String.valueOf(id );
		}
	}

}
