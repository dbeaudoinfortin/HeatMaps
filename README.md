# HeatMaps

Easily create beautiful custom heat maps in only a few lines of Java code. 

I orginally developed this code for the [NAPS Data Analysis Toolbox](https://github.com/dbeaudoinfortin/NAPSDataAnalysis) and now I have spun it out into its own project so others can also benefit from it.


## How To Use

Add the following to your pom.xml:

```xml
<dependency>
  <groupId>io.github.dbeaudoinfortin</groupId>
  <artifactId>heatmaps</artifactId>
  <version>0.0.1</version>
</dependency>
```

And invoke the HeatMap rendering by calling the rend() method:

```java
HeatMap.builder()
  .withTitle(shortTitle)
  .withXAxis(xAxis)
  .withYAxis(yAxis)
  .withOptions(HeatMapOptions.builder()
    .withGradient(HeatMapGradient.BASIC_GRADIENT)
    .build())
  .build()
  .render(myOutPutFile, myDataRecords);
```

(More documentation coming soon.)

## Examples

<p align="center">
  <img src="https://github.com/user-attachments/assets/00434021-e13b-4797-b5cb-4e236c591a29" width="800" />
  <img src="https://github.com/user-attachments/assets/b814eb8f-5e93-4184-a764-a91d6f990880" width="400" />
  <img src="https://github.com/user-attachments/assets/650c4387-c5b4-4005-ad42-3c344b5436ba" width="400" />
  <img src="https://github.com/user-attachments/assets/373d0966-0c9a-4739-b014-c8b2ddf6c1e6" width="400" />
</p>

(More varied examples coming soon.)

## Customization Options

The heat maps are highly customizable, down to the colour, fonts, gradients, titles, layout, etc.

(Documentation coming soon.)

## How It Works

(Documentation coming soon.)

## Requirements

- Requires Java 8 or later.
- If you want to build the jar from the source code you will need [Apache Maven](https://maven.apache.org/).

## Legal Stuff

Copyright (c) 2024 David Fortin

This software is provided by David Fortin under the MIT License, meaning you are free to use it however you want, as long as you include the original copyright notice (above) and license notice in any copy you make. You just can't hold me liable in case something goes wrong. License details can be read [here](https://github.com/dbeaudoinfortin/heatmaps?tab=MIT-1-ov-file)
