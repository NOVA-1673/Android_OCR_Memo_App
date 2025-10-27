package com.ex.realcv.TodoMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceManager {
    List<SpaceLayer> layers = new ArrayList<>();
    Random random = new Random();

    // 레이어 초기화 (예: 10개, 5 단위 거리마다)
    public void initLayers(int numLayers, float spacing) {
        for (int i = 1; i <= numLayers; i++) {
            layers.add(new SpaceLayer(i * spacing));
        }
    }

    // 행성 생성
    public void generatePlanets(int planetsPerLayer) {
        for (SpaceLayer layer : layers) {
            for (int i = 0; i < planetsPerLayer; i++) {
                // 구 좌표계로 랜덤 배치
                double theta = random.nextDouble() * 2 * Math.PI;
                double phi = Math.acos(2 * random.nextDouble() - 1);

                float x = (float)(layer.radius * Math.sin(phi) * Math.cos(theta));
                float y = (float)(layer.radius * Math.sin(phi) * Math.sin(theta));
                float z = (float)(layer.radius * Math.cos(phi));

                float size = 0.5f + random.nextFloat(); // 크기 랜덤 (0.5~1.5)

                float[] color = new float[]{
                        random.nextFloat(),   // R
                        random.nextFloat(),   // G
                        random.nextFloat(),   // B
                        1.0f                  // A
                };

                Planet planet = new Planet(x, y, z, size);
                layer.addPlanet(planet);
            }
        }
    }

    // 카메라 위치 근처의 행성만 가져오기
    public List<Planet> getNearbyPlanets(float camX, float camY, float camZ) {
        float camDist = (float)Math.sqrt(camX*camX + camY*camY + camZ*camZ);
        List<Planet> nearby = new ArrayList<>();

        for (SpaceLayer layer : layers) {
            if (Math.abs(layer.radius - camDist) < 10f) { // 카메라 반경 ±10 안쪽만 탐색
                nearby.addAll(layer.planets);
            }
        }
        return nearby;
    }

    // 전체 행성 리스트 가져오기 (렌더링 용도)
    public List<Planet> getAllPlanets() {
        List<Planet> all = new ArrayList<>();
        for (SpaceLayer layer : layers) {
            all.addAll(layer.planets);
        }
        return all;
    }
}
