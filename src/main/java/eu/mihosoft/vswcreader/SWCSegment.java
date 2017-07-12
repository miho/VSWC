/*
 * Copyright 2017 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * If you use this software for scientific research then please cite the following publication(s):
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */
package eu.mihosoft.vswcreader;

import eu.mihosoft.vvecmath.Vector3d;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SWC segment. This class provides methods for loading and saving SWC formatted
 * data.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public final class SWCSegment {

    private int index;
    private Vector3d pos;
    private double r;
    private final int type;
    private int parent;
    private Vector3d normal;
    private List<String> comments = null;

    /**
     * Creates a new SWC segment.
     *
     * @param index segment index (SWC indices usually start with 1)
     * @param type segment type
     * @param pos position in space
     * @param r radius
     * @param parent parent segment (indices &lt; 0 indicate that no parent is
     * present)
     */
    public SWCSegment(int index, int type, Vector3d pos, double r, int parent) {
        this.index = index;
        this.pos = pos;
        this.r = r;
        this.type = type;
        this.parent = parent;
    }

    /**
     * Copy constructor. Performs a deep copy of the specified segment.
     *
     * @param other segment to copy
     */
    public SWCSegment(SWCSegment other) {
        this.index = other.index;
        this.pos = other.pos;
        this.r = other.r;
        this.type = other.type;
        this.parent = other.parent;
        this.normal = other.normal;
        if (other.comments != null && !other.comments.isEmpty()) {
            this.comments = new ArrayList<>(other.comments);
        }
    }

    /**
     * Returns the segment index.
     *
     * @return segment index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the parent index (range: [-1,n])
     *
     * @return parent index (indices &lt; indicate that no parent is present)
     */
    public int getParent() {
        return parent;
    }

    /**
     * Sets the parent index (range: [-1,n])
     *
     * @param parent parent index (indices &lt; indicate that no parent is
     * present)
     */
    public void setParent(int parent) {
        this.parent = parent;
    }

    /**
     * Returns the position in space
     *
     * @return position vector
     */
    public Vector3d getPos() {
        return pos;
    }

    /**
     * Returns the radius of this segment.
     *
     * @return radius
     */
    public double getR() {
        return r;
    }

    /**
     * Returns the segment type
     *
     * @return segment type
     */
    public int getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SWCSegment other = (SWCSegment) obj;
        if (this.index != other.index) {
            return false;
        }
        if (Double.compare(this.r, other.r) != 0) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.parent != other.parent) {
            return false;
        }
        if (!Objects.equals(this.pos, other.pos)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.index;
        hash = 83 * hash + Objects.hashCode(this.pos);
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.r) ^ (Double.doubleToLongBits(this.r) >>> 32));
        hash = 83 * hash + this.type;
        hash = 83 * hash + this.parent;
        return hash;
    }

    /**
     * Reads SWC segments from specified stream.
     *
     * @param i input stream to read from
     * @return list of SWC segments
     * @throws IOException if reading failed due to I/O related errors
     * @throws RuntimeException if reading failed due to file format related
     * errors
     */
    public static List<SWCSegment> fromStream(InputStream i) throws IOException {
        List<SWCSegment> segments = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(i))) {
            List<String> comments = new ArrayList<>();
            while (br.ready()) {
                String line = br.readLine().trim();
                if (line.startsWith("#")) {
                    comments.add(line);
                } else if (!line.isEmpty()) {
                    SWCSegment swcSegment = SWCSegment.fromLine(line);
                    if (!comments.isEmpty()) {
                        swcSegment.withComments(comments);
                        comments.clear();
                    }
                    segments.add(swcSegment);
                }
            }
            if (!comments.isEmpty()) {
                Logger.getLogger(SWCSegment.class.getName()).
                        log(Level.WARNING, "Skipping comments at end of file!");
            }
        }

        return segments;
    }

    /**
     * Reads SWC segments from specified string.
     *
     * @param s string to parse
     * @return list of segments
     * @throws IOException if reading failed due to I/O related errors
     * @throws RuntimeException if reading failed due to file format related
     * errors
     */
    public static List<SWCSegment> fromString(String s) throws IOException {
        return SWCSegment.fromStream(
                new ByteArrayInputStream(s.getBytes("UTF-8")));
    }

    /**
     * Reads an swc segment from string.
     *
     * @param s string to read
     * @return segment
     * @throws RuntimeException if the string cannot be read
     */
    public static SWCSegment fromLine(String s) {

        String[] elements = s.trim().split("\\s+");
        if (elements.length != 7) {
            throw new RuntimeException(
                    "Expected 7 elements separated by regex '\\s+', got "
                    + elements.length + " in line '" + s + "'.");
        }

        String swcElementType = "";
        String swcElementString = "";
        int swcElementIndex = -1;
        try {
            swcElementType = "index";
            swcElementString = elements[0].trim();
            int index = Integer.parseInt(swcElementString);
            swcElementIndex = index;
            swcElementType = "element type";
            swcElementString = elements[1].trim();
            int type = Integer.parseInt(swcElementString);
            swcElementType = "x coordinate";
            swcElementString = elements[2].trim();
            double x = Double.parseDouble(swcElementString);
            swcElementType = "y coordinate";
            swcElementString = elements[3].trim();
            double y = Double.parseDouble(swcElementString);
            swcElementType = "z coordinate";
            swcElementString = elements[4].trim();
            double z = Double.parseDouble(swcElementString);
            swcElementType = "radius";
            swcElementString = elements[5].trim();
            double r = Double.parseDouble(swcElementString);
            swcElementType = "parent index";
            swcElementString = elements[6].trim();
            int parent = Integer.parseInt(swcElementString);

            return new SWCSegment(
                    index, type, Vector3d.xyz(x, y, z), r, parent);

        } catch (NumberFormatException ex) {
            String elementError = "element " + swcElementIndex;
            if (swcElementType.equals("index")) {
                elementError = "the current segment";
            }

            throw new RuntimeException(
                    "Error while parsing entry '" + swcElementType
                    + "' of " + elementError + ". Entry string: '"
                    + swcElementString + "'");
        }
    }

    public SWCSegment withComments(List<String> comments) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.addAll(comments);

        return this;
    }

    /**
     * Returns the normal of this segment.
     *
     * @return normal vector
     */
    public Vector3d getNormal() {
        return normal;
    }

    /**
     * Sets the normal of this segment.
     *
     * @param normal normal vector
     */
    public void setNormal(Vector3d normal) {
        this.normal = normal;
    }

    /**
     * Sets the radius of this segment.
     *
     * @param r radius of this segment
     */
    public void setR(double r) {
        this.r = r;
    }

    @Override
    public String toString() {
        return "index: " + getIndex() + ", segment type: " + getType()
                + ", pos: " + getPos() + ", radius: " + getR() + ", parent: "
                + getParent();
    }

    public String toSWCString() {
        String commentString = String.join("\n",
                comments == null ? Collections.emptyList() : comments);
        String data = getIndex() + " " + getType() + " " + getPos().x()
                + " " + getPos().y() + " " + getPos().z() + " " + getR()
                + " " + getParent();

        return commentString + (commentString.isEmpty() ? "" : "\n") + data;
    }

    /**
     * Writes the specified SWC segments to an output stream.
     *
     * @param out output stream
     * @param segments segments to write
     * @return specified output stream
     * @throws IOException if an IO related error occures
     */
    public static OutputStream toStream(OutputStream out,
            List<SWCSegment> segments)
            throws IOException {
        try (BufferedWriter writer
                = new BufferedWriter(new OutputStreamWriter(out))) {
            for (SWCSegment s : segments) {
                writer.append(s.toSWCString());
                writer.newLine();
            }
        }

        return out;
    }

    /**
     * Writes the specified SWC segments to an output stream.
     *
     * @param segments segments to write
     * @return string that contains the specified SWC data
     */
    public static String toSWCString(List<SWCSegment> segments) {

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            toStream(bout, segments);
            return bout.toString("UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(SWCSegment.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return "";
    }

    public List<String> getComments() {

        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }

        return comments;
    }

    /**
     * Sets the SWC index of this segment. Usually, indices start with 1.
     *
     * @param index index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Sets the position of this segment.
     *
     * @param pos position to set
     */
    public void setPos(Vector3d pos) {
        this.pos = pos;
    }

}
