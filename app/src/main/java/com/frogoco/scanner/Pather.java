package com.frogoco.scanner;

import android.graphics.Point;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Pather {
    ArrayList<String> values;
    ArrayList<ArrayList<String>> lines;
    int numBuff;
    ArrayList<Coord> usedCoords;
    class Coord {
        public int row;
        public int col;

        public boolean cEquals(Coord comp) {
            if (row != comp.row) {
                return false;
            }
            if (col != comp.col) {
                return false;
            }
            return true;
        }
    }

    protected void onCreate(ArrayList<String> inValues, ArrayList<ArrayList<String>> sequences, int buffSize) {
        values = inValues;
        numBuff = buffSize;
        makeLines();
        usedCoords = new ArrayList<>();
    }

    private void makeLines() {
        int lineLen = 0;
        int valInd = 0;
        if (values.size() == 16) {
            lineLen = 4;
        } else if (values.size() == 25) {
            lineLen = 5;
        } else if (values.size() == 36) {
            lineLen = 6;
        }
        for (int i = 0; i < lineLen; i++) {
            ArrayList<String> newline = new ArrayList<>();
            for (int j = 0; j < lineLen; j++) {
                newline.add(values.get(valInd));
                valInd++;
            }
            lines.add(newline);
        }
    }

    private boolean checkSeq(ArrayList<String> seq, Coord startCo) {
        ArrayList<Coord> tUsedCoords = new ArrayList<>();
        int workRow = startCo.row;
        int workCol = startCo.col;
        for (String val : seq) {

        }
        return false;

    }

    private Coord findNext(Coord orig, boolean isVert, String seqVal,
                           ArrayList<Coord> tUsedCoords) {
        Coord curco = new Coord();
        curco.col = -1;
        curco.row = -1;
        if (!isVert) {
            ArrayList<String> curLine = lines.get(orig.row);
            for (String val : curLine) {
                if (val.equals(seqVal)) {
                    Coord tempCo = new Coord();
                    tempCo.col = curLine.indexOf(val);
                    tempCo.row = orig.row;
                    boolean checked = false;
                    for (Coord prevco : tUsedCoords) {
                        if (prevco.cEquals(tempCo)) {
                            checked = true;
                        }
                    }
                    if (!checked) {
                        curco.col = curLine.indexOf(val);
                        curco.row = orig.row;
                    }
                }
            }
        } else {
            for (ArrayList<String> line : lines) {
                if (seqVal.equals(line.get(orig.col))) {
                    Coord tempCo = new Coord();
                    tempCo.col = orig.col;
                    tempCo.row = lines.indexOf(line);
                    boolean checked = false;
                    for (Coord prevco : tUsedCoords) {
                        if (prevco.cEquals(tempCo)) {
                            checked = true;
                        }
                    }
                    if (!checked) {
                        curco.col = orig.col;
                        curco.row = lines.indexOf(line);
                    }
                }
            }
        }
        return curco;
    }
}
