/*
 * Copyright (c) 2019 R.C
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.toy;

import java.util.HashMap;
import java.util.Map;

public class MarsRover {

  static class Plateau {

    int border_x;
    int border_y;

    Plateau(int border_x, int border_y) {
      this.border_x = border_x;
      this.border_y = border_y;
    }

    boolean validate(Location loc) {
      return (0 <= loc.getX() && loc.getX() <= border_x) &&
             (0 <= loc.getY() && loc.getY() <= border_y);
    }

  }

  static class Location {

    int x;
    int y;

    Location(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    @Override public String toString() {
      return "{" + "x=" + x + ", y=" + y + '}';
    }

  }

  enum Direction {

    E(1, 0),
    S(0, -1),
    W(-1, 0),
    N(0, 1);

    Direction(int delta_x, int delta_y) {
      this.delta_x = delta_x;
      this.delta_y = delta_y;
    }

    private int delta_x;
    private int delta_y;

    Location moveFrom(Location loc) {
      return new Location(loc.getX() + delta_x, loc.getY() + delta_y);
    }

    Direction turnLeft() {
      if (this == E) return N;
      if (this == S) return E;
      if (this == W) return S;
                     return W;
    }

    Direction turnRight() {
      if (this == E) return S;
      if (this == S) return W;
      if (this == W) return N;
                     return E;
    }

    static Direction parseDirection(String dir) {
      String ud = dir.toUpperCase();
      switch (ud) {
        case "E": return E;
        case "S": return S;
        case "W": return W;
        case "N": return N;
        default: throw new IllegalArgumentException();
      }
    }

    @Override public String toString() {
      return "{" + name() + '}';
    }

  }

  static class Rover {

    Location location;
    Direction direction;

    Rover(Location location, Direction direction) {
      this.location = location;
      this.direction = direction;
    }

    void move(Plateau plateau, char... moves) {
      for (char c : moves) {
        char uc = Character.toUpperCase(c);
        switch (uc) {
          case 'L': direction = direction.turnLeft();  break;
          case 'R': direction = direction.turnRight(); break;
          case 'M': {
            Location newLoc = direction.moveFrom(location);
            if (plateau.validate(newLoc)) {
              location = newLoc;
            }
            break;
          }
          default: break;
        }
      }
    }

    @Override public String toString() {
      return "Rover{" + "location=" + location + ", direction=" + direction + '}';
    }

  }

  private Map<Rover, String> inputs = new HashMap<>();
  private Plateau plateau;

  public void init(String[] args) {
    String[] plateauPoint = args[0].split(" ");
    assertValid(plateauPoint, 2);
    plateau = new Plateau(
        Integer.parseInt(plateauPoint[0]),
        Integer.parseInt(plateauPoint[1])
    );

    for (int i = 1; i < args.length; i += 2) {
      String[] rover = args[i].split(" ");
      assertValid(rover, 3);
      inputs.put(
          new Rover(
              new Location(
                  Integer.parseInt(rover[0]),
                  Integer.parseInt(rover[1])
              ),
              Direction.parseDirection(rover[2])
          ),
          args[i + 1]);
    }
  }

  public void start() {
    for (Map.Entry<Rover, String> entry : inputs.entrySet()) {
      Rover rover      = entry.getKey();
      String movements = entry.getValue();
      System.out.println(rover);
      rover.move(plateau, movements.toCharArray());
      System.out.println(rover);
      System.out.println();
    }
  }

  private void assertValid(String[] res, int expected) {
    if (res.length != expected) {
      throw new IllegalArgumentException();
    }
  }

  public static void main(String[] args) {
    args = new String[] {
        "5 5",
        "1 2 N", "LMLMLMLMM", "3 3 E", "MMRMMRMRRM", "0 0 S", "MMMLLLMMMMMMMM",
        "2 3 N", "LLLL", "3 2 S", "RRRR"
    };

    if (args.length > 1 && args.length % 2 == 1) {
      MarsRover rover = new MarsRover();
      rover.init(args);
      rover.start();
    }
  }

}
