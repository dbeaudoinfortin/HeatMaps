# HeatMaps

Easily create beautiful custom heat maps in only a few lines of Java code!

I orginally developed this code for the [NAPS Data Analysis Toolbox](https://github.com/dbeaudoinfortin/NAPSDataAnalysis) and now I have spun it out into its own project so others can also benefit from it.

## Getting Started

Add the following to your `pom.xml`:

```xml
<dependency>
  <groupId>io.github.dbeaudoinfortin</groupId>
  <artifactId>heatmaps</artifactId>
  <version>0.0.1</version>
</dependency>
```

Customize your heat map using the builder class and render it by calling `HeatMap.render()`:

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
The output will be a PNG file at the output path of `myOutPutFile`.

The builder pattern makes it easy to customize your heat map. For example, the following will render a heat map with no titles, no labels, no legend no border, just the core heat map with blending enabled:

```java
HeatMap.builder()
  .withTitle("")
  .withXAxis(xAxis)
  .withYAxis(yAxis)
  .withOptions(HeatMapOptions.builder()
    .withShowLegend(false)
    .withShowXAxisLabels(false)
    .withShowYAxisLabels(false)
    .withOutsidePadding(0)
    .withBlendColours(true)
    .withBlendColoursScale(5)
    .withColourScaleLowerBound(5)
    .withColourScaleUpperBound(20)
    .withGradient(HeatMapGradient.getCannedGradient(5)
    .build())
  .build()
  .render(myOutPutFile, myDataRecords);
```

The data is passed in as a `Collection<DataRecord>`. You can either use your own pojo and implement the interface `com.dbf.heatmaps.data.DataRecord` or use the provided `com.dbf.heatmaps.data.BasicDataRecord` pojo class. The interface is very simple:

```java
public interface DataRecord {
  public Double getValue();
  public Object getX();
  public Object getY();
}
```

Two options are provided for defining the chart axes: `IntegerAxis` which is useful integer values (such as 1-31 for days of the month) and `StringAxis` which is useful for string values (such as Monday-Sunday for days of the week). In both cases, the axis entries are always treated as discrete values and rendered in the same order that each entry is added to the axis. For the `IntegerAxis`, a convenience method is provided to automatically populate the values between a given minimum and maximum. For example:

```java
IntegerAxis xAxis = new IntegerAxis("Days of the Year", 1, 366);
StringAxis yAxis = new StringAxis("Weird Cars", "BMC Landcrab", "Ford Probe", "Renault LeCar", "Subaru Brat", "Ferrari LaFerrari");
```

## Examples

<p align="center">
  <img src="https://github.com/user-attachments/assets/00434021-e13b-4797-b5cb-4e236c591a29" width="800" />
  <img src="https://github.com/user-attachments/assets/b814eb8f-5e93-4184-a764-a91d6f990880" width="400" />
  <img src="https://github.com/user-attachments/assets/650c4387-c5b4-4005-ad42-3c344b5436ba" width="400" />
  <img src="https://github.com/user-attachments/assets/373d0966-0c9a-4739-b014-c8b2ddf6c1e6" width="400" />
</p>

## Customization Options

The heat maps are highly customizable, down to colours, fonts, gradients, titles, layout, etc. The following customization options are provided out of the box:

**Titles, Labels, Legend**
- Option to display titles for the X-axis, Y-axis, and the overall chart.
- Custom fonts and colours for all titles
- Option to display labels for the X-axis and Y-axis
- Custom fonts and colours for the axis labels
- Option to display a legend, with custom upper and lower bounds and custom number formatting
- Custom padding between all the elements (titles, labels, legend, etc.) 

**Heat Map Rendering**
- Custom colour gradients using either pre-defined steps with linear interpolation, or smooth a smooth gradient based on the HSB model colour wheel
- 9 pre-defined gradients to choose from if you don't want to define your own
- Option to blend the colours of each cell using a custom scale factor for applying the linear colour interpolation 
- Custom background colour
- Option to render of grid lines, including thickness and colour
- Cell width and height

## Pre-Defined Heat Map Gradients

There are 9 pe-defined gradients to choose from. I plan to eventually add more in the future. The current palettes are the following:
1. A smooth gradient based on the colour wheel from blue to red. All the of the colours are fully saturated.
2. A 12 step gradient from blue to red with less saturation than the first colour palette.
3. A simplified 5 step gradient from blue to red.
4. A two colour gradient from blue to red, with purple mixed in-between.
5. A 5 step colour blind friendly gradient of greenish-yellow to dark orange.
6. A 3 step black-red-orange gradient, similar to black-body radiation, up to approximately 1300 degrees kelvin.
7. Same as number 6 but two more steps are added to extend the scale up to approximately 6500k degrees kelvin.
8. A 50 step colour gradient based on [Dave Green's ‘cubehelix’ colour scheme](https://people.phy.cam.ac.uk/dag9/CUBEHELIX/)
9. A 2 step grey-scale gradient that should be used for non-colour screen/print-outs.

The default colour palette, if not specified, is set to number 3. Here are examples of what the colour palette look like, in order:

![Gradient_1](https://github.com/user-attachments/assets/5752f84b-8e6c-4164-9631-1f0430230471)
![Gradient_2](https://github.com/user-attachments/assets/bdcd777e-2dda-4a1b-94b0-8cb113a59ac0)
![Gradient_3](https://github.com/user-attachments/assets/b9402f99-a82d-4163-8d27-4be4eef6812c)
![Gradient_4](https://github.com/user-attachments/assets/b582757d-824a-4f61-8410-8451b158e1ad)
![Gradient_5](https://github.com/user-attachments/assets/4d9c962d-33bd-4de8-86b7-77e55506312c)
![Gradient_6](https://github.com/user-attachments/assets/8ccccdc7-25fe-4847-8bbd-76e34c9a8af4)
![Gradient_7](https://github.com/user-attachments/assets/54b074bc-b101-4813-a54c-db6030c81c4a)
![Gradient_8](https://github.com/user-attachments/assets/397e97b8-8b01-4261-bd44-16aa1614667b)
![Gradient_9](https://github.com/user-attachments/assets/b8ac0f6a-7481-4851-9f3b-b229a4c5f059)


## Colour Blending

Enabling the `blendColours` option will result in a linear colour interpolation being applied to the cells of the heat map in order to "smooth out" the values. This effect works by first rendering each cell as a single pixel, then applying a bilinear upscaler (with a variable scaling factor), followed by applying an alpha mask to avoid blending with empty cells, and finally applying a nearest-neighbour up or down scaling to the final output resolution. The strength of this effect can be controlled via the `blendColoursScale` option, which supports values between 2 and 20, inclusive. By default, a value of 3 is used which will upscale each cell to a 3x3 grid, ensuring that the centre of the grid always represents the true colour of the corresponding data. Here is an example of what colour blending looks like, starting with no effect, then a strength of 2, 3, 5,and 10, respectively.

<p align="center">
  <img src="https://github.com/user-attachments/assets/f64e9520-7f8b-4473-be04-34be30771b69" width="200" />
  <img src="https://github.com/user-attachments/assets/1b3cdab1-97a9-4e4a-9f47-670644505911" width="200" />
  <img src="https://github.com/user-attachments/assets/6ecc18c4-0482-4f50-ad7d-a1f0d5dbf9f7" width="200" />
  <img src="https://github.com/user-attachments/assets/259f2575-b1f9-4aa1-9962-969fa3b6ed7c" width="200" />
  <img src="https://github.com/user-attachments/assets/657d4e69-3a81-47a2-84ca-e1bbafcbd57c" width="200" />
</p>

## How It Works

This library is built from scratch using the Java 2D graphics classes. To make it easy to integrate with your project, there are no external dependencies, only Java 8 or later.

(more documentation coming soon.)

## Requirements

- Requires Java 8 or later.
- If you want to build the jar from the source code you will need [Apache Maven](https://maven.apache.org/).

## Legal Stuff

Copyright (c) 2024 David Fortin

This software is provided by David Fortin under the MIT License, meaning you are free to use it however you want, as long as you include the original copyright notice (above) and license notice in any copy you make. You just can't hold me liable in case something goes wrong. License details can be read [here](https://github.com/dbeaudoinfortin/heatmaps?tab=MIT-1-ov-file)
