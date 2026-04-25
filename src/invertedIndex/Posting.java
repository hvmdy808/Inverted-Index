/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.util.ArrayList;

/**
 *
 * @author ehab
 */
public class Posting {

    public Posting next = null;
    int docId;
    int dtf = 1;
    public ArrayList<Integer> positions;

    Posting(int id, int firstPosition) {
        docId = id;
        dtf = 1;
        positions = new ArrayList<>();
        positions.add(firstPosition);
    }

    Posting(int id) {
        docId = id;
        dtf = 1;
        positions = new ArrayList<>();
    }

    public void addPosition(int position) {
        this.positions.add(position);
        this.dtf++;
    }
}