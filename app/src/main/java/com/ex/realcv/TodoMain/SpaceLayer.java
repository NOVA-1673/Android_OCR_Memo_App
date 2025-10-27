package com.ex.realcv.TodoMain;

import java.util.ArrayList;
import java.util.List;

public class SpaceLayer {
    float radius;              // 이 레이어의 중심 반경
    List<Planet> planets;      // 해당 반경에 속한 행성들

    public SpaceLayer(float radius) {
        this.radius = radius;
        this.planets = new ArrayList<>();
    }

    public void addPlanet(Planet planet) {
        planets.add(planet);
    }
}
