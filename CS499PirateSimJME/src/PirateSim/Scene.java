
        lighting = new Lighting();

        Vector3f lightDir = new Vector3f(0, -1, 0);


            //fpp.addFilter(new TranslucentBucketFilter());
            //com.jme3.post.filters.TranslucentBucketFilter


            water.setWaveScale(0.03f);
            water.setMaxAmplitude(0.5f);
            water.setFoamExistence(new Vector3f(1f, 4, 0.1f));
            water.setNormalScale(20f);
            water.setShininess(0.15f);
            //water.setSunScale(1f);
            water.setColorExtinction(new Vector3f(2f, 5f, 6f));
            water.setDeepWaterColor(new ColorRGBA(0.03f, 0.06f, 0.1f, 1f));
            water.setRadius(SEA_BORDER_SIZE);
            water.setWaterColor(new ColorRGBA(0.4f, 0.9f, 1f, 1f));
            water.setWindDirection(Vector2f.UNIT_XY);
            water.setRefractionConstant(0.25f);
            water.setRefractionStrength(0.1f);
            
            Spatial terrain = assetMan.loadModel("Models/terrain/terrain.j3o");
            Material terrainMat = new Material(assetMan, "Common/MatDefs/Light/Lighting.j3md");
            terrainMat.setTexture("DiffuseMap", assetMan.loadTexture("Textures/gulf of aden color.png"));
//            terrainMat.setBoolean("UseMaterialColors", true);
//            terrainMat.setColor("Diffuse", ColorRGBA.White);
//            terrainMat.setColor("Specular", ColorRGBA.White);
//            terrainMat.setFloat("Shininess", 2f);  // [0,128]
            
            terrain.setMaterial(terrainMat);
            
            Quad ground = new Quad(sim.size.x + SEA_BORDER_SIZE*2, sim.size.y + SEA_BORDER_SIZE*2);
            seaGeom.center().getLocalTranslation().set(-SEA_BORDER_SIZE, -3f, -SEA_BORDER_SIZE);
            Grid mapGrid = new Grid(sim.size.y + 1, sim.size.x + 1, 1);
        Lighting() {
            //rootNode.attachChild(lightingNode);
            rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
               moonLight.setColor(moonColor.mult(moonIntensity));
               moonAmbient.setColor(moonAmbColor.mult(.5f));
               angle = moonAngle;
               System.out.println("moon " + angle);