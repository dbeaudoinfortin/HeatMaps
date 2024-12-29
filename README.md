# HeatMaps

Easily create beautiful custom heat maps in only a few lines of Java code!

I originally developed this code for the [NAPS Data Analysis Toolbox](https://github.com/dbeaudoinfortin/NAPSDataAnalysis) and now I have spun it out into its own project so others can also benefit from it.

## Getting Started

Add the following to your `pom.xml`:

```xml
<dependency>
  <groupId>io.github.dbeaudoinfortin</groupId>
  <artifactId>heatmaps</artifactId>
  <version>0.0.3</version>
</dependency>
```

Customize your heat map using the builder class and render it by calling `HeatMap.render()`:

```java
HeatMap.builder()
  .withTitle("My New Chart")
  .withXAxis(xAxis)
  .withYAxis(yAxis)
  .withOptions(HeatMapOptions.builder()
    .withGradient(HeatMapGradient.BASIC_GRADIENT)
    .build())
  .build()
  .render(myOutputFile, myDataRecords);
```
The output will be a PNG file at the output path of `myOutputFile`.

The builder pattern makes it easy to customize your heat map. For example, the following will render a heat map with no titles, no labels, no legend no border, just the core heat map with blending enabled and upper & lower bounds set:

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
    .withColourScaleLowerBound(5.0)
    .withColourScaleUpperBound(20.0)
    .withGradient(HeatMapGradient.getCannedGradient(5))
    .build())
  .build()
  .render(myOutputFile, myDataRecords);
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
StringAxis  yAxis = new StringAxis("Weird Cars", "BMC Landcrab", "Ford Probe", "Renault LeCar", "Subaru Brat", "Ferrari LaFerrari");
```

## Examples

<p align="center">
  <img src="https://github.com/user-attachments/assets/998bc959-98e0-402d-8a2e-504b01f2d917" width="400" />
  <img src="https://github.com/user-attachments/assets/2ce8e484-b4ab-429a-8c59-d2b79cd66107" width="400" />
  <img src="https://github.com/user-attachments/assets/30dfa516-0e8c-4327-8609-3e6747a19e09" width="400" />
  <img src="https://github.com/user-attachments/assets/6164081d-b6f3-4fd3-a210-3ca05074cb94" width="400" />
  <img src="https://github.com/user-attachments/assets/00434021-e13b-4797-b5cb-4e236c591a29" width="800" />
  <img src="https://github.com/user-attachments/assets/b814eb8f-5e93-4184-a764-a91d6f990880" width="400" />
  <img src="https://github.com/user-attachments/assets/650c4387-c5b4-4005-ad42-3c344b5436ba" width="400" />
</p>

## Customization Options

The heat maps are highly customizable, down to colours, fonts, gradients, titles, layout, etc. The following customization options are provided out of the box:

**Heat Map Rendering**
- Custom colour gradients using either pre-defined steps with linear interpolation, or smooth a smooth gradient based on the HSB model colour wheel
- 9 pre-defined gradients to choose from if you don't want to define your own
- Option to blend the colours of each cell using a custom scale factor for applying the linear colour interpolation 
- Custom background colours
- Option to render grid lines, including customizing the thickness and colour
- Option to print data values on top of the heat map, with custom formatting, font, and colour
- Custom cell width and height
- Custom padding between all the elements (titles, labels, legend, etc.)

**Titles & Labels**
- Option to display titles for the X-axis, Y-axis, and the overall chart
- Custom fonts and colours for all titles
- Option to display labels for the X-axis and Y-axis
- Custom fonts and colours for the axis labels
- Labels for the X-axis can be rotated and can be rendered either above or below the heat map

**Legend**
- Option to display a legend
- Custom custom upper and lower bounds
- Custom number formatting for legend labels
- Custom fonts and colours for the legend labels
- Configurable number of discrete colour steps

All of these options can be set in the `HeatMapOptions` class. The following table summarizes all the possible configuration options.

|Option|Default Value|Description|
|-|-|-|
|backgroundColour|White, unless it conflicts with the colour gradient, then grey.|Sets the background colour of the whole chart.|
|cellWidth|50px|Sets the desired cell width in pixels. Will be enlarged if it's too small to fit the labels. Minimum is 1.|
|cellHeight|50px|Sets the desired cell width in pixels . Will be enlarged if it's too small to fit the labels. Minimum is 1.|
|showGridlines|false|Toggles the rendering of grid lines on the heat map between the cells.|
|gridLineWidth|1px|Sets the width, in pixels, of the grid lines.|
|gridLineColour|Black|Sets the colour of the grid lines.|
|showXAxisLabels|true|Toggles the rendering of the labels for the X-axis.|
|xAxisLabelsBelow|false|Toggles the rendering of the X-axis labels below the heat map instead of above.|
|xAxisLabelsRotate|false|Forces the X-axis labels to be rotated 90 degrees and rendered vertically. Otherwise, the X-axis labels will be automatically rotated if they are bigger than the cell width. When the labels are rendered above the heat map they are vertically aligned to the bottom, and when they are rendered below the heat map they are vertically aligned to the top.|
|showYAxisLabels|true|Toggles the rendering of the labels for the Y-axis.
|axisLabelFont|Calibri, Plain, 20pts|Sets the font used to render the X-axis and Y-axis labels.|
|axisLabelFontColour|Black|Sets the colour used to render the X-axis and Y-axis labels.|
|axisTitleFont|Calibri, Bold, 20pts|Sets the font used to render the X-axis and Y-axis titles.|
|axisTitleFontColour|Black|Sets the colour used to render the X-axis and Y-axis titles.|
|heatMapTitleFont|Calibri, Bold, 36pts|Sets the font used to render the overall chart title.|
|heatMapTitleFontColour|Black|Sets the colour used to render the overall chart title.|
|showGridValues|false|Toggles the rendering of the values within each cell of the heat map.|
|gridValuesFormat|0.#|Sets the decimal format used to display the values within each cell of the heat map. The `Double` to `String` conversion makes use of the Java `DecimalFormat` class.|
|gridValuesFont|Calibri, Plain, 20pts|Sets the font used to render the values within each cell of the heat map.|
|gridValuesFontColour|Black|Sets the colour used to render the values within each cell of the heat map.|
|blendColours|false|Toggles the blending of colours between adjacent cells of the heat map grid. See the [section below](#colour-blending) for details on how this works.|
|blendColoursScale|3|Sets the amount (strength) of blending to use. This corresponds to the scaling factor of the bilinear interpolation, essentially how "smooth" the result will be. Minimum is 2, maximum is 20. See the [section below](#colour-blending) for details on how this works.|
|axisLabelPadding|10px|Sets the amount of blank space (padding), in pixels, between the X-axis and Y-axis labels and the heat map grid.|
|axisTitlePadding|20px|Sets the amount of blank space (padding), in pixels, below (for the X-axis) and to the right (for the Y-axis) of the axis titles.|
|heatMapTitlePadding|40px|Sets the amount of blank space (padding), in pixels, below the overall chart title.|
|outsidePadding|5px|Sets the amount of blank space (padding), in pixels, on the perimeter of the entire chart.|
|legendPadding|40px|Sets the amount of blank space (padding), in pixels, between the legend and the heat map grid.|
|showLegend|true|Toggles the rendering of the legend, including the legend labels.|
|legendTextFormat|0.##|Sets the decimal format used to display the values of the legend labels. The `Double` to `String` conversion makes use of the Java `DecimalFormat` class.
|legendLabelFont|Calibri, Plain, 20pts|Sets the font used to render the legend labels.|
|legendLabelFontColour|Black|Sets the colour used to render the legend labels.|
|legendSteps|The greater of the number of cells of the Y-axis and 5|Sets the number of discrete colour steps to include in the legend. The minimum value is 2.|
|gradient|`HeatMapGradient.BASIC_GRADIENT`|Sets the colour gradient for the heat map. See the [section below](#heat-map-gradients) for details on how this works.|
|colourScaleLowerBound|Automatically calculated based on the lowest data value.|Restricts the minimum value (low bound) of the heat map gradient. Any value below this threshold will be assigned the same minimum colour according to the chosen gradient.|
|colourScaleUpperBound|Automatically calculated based on the highest data value.|Restricts the maximum value (upper bound) of the heat map gradient. Any value above this threshold will be assigned the same maximum colour according to the chosen gradient.|

## Heat Map Gradients

There are 9 pre-defined gradients to choose from. I plan to eventually add more in the future. The current palettes are the following:
1. A smooth gradient based on the colour wheel from blue to red. All of the colours are fully saturated.
2. A 12 step gradient from blue to red with less saturation than the first colour palette.
3. A simplified 5 step gradient from blue to red.
4. A two colour gradient from blue to red, with purple mixed in-between.
5. A 5 step colour blind friendly gradient of greenish-yellow to dark orange.
6. A 3 step black-red-orange gradient, similar to black-body radiation, up to approximately 1300 degrees Kelvin.
7. Same as number 6 but two more steps are added to extend the scale up to approximately 6500k degrees Kelvin.
8. A 50 step colour gradient based on [Dave Green's ‘cubehelix’ colour scheme](https://people.phy.cam.ac.uk/dag9/CUBEHELIX/)
9. A 2 step grey-scale gradient that should be used for non-colour screen/print-outs.

The default colour palette, if not specified, is set to number 3. Here are examples of what the colour palettes look like, in order:

<p align="center">
<img src="https://github.com/user-attachments/assets/5752f84b-8e6c-4164-9631-1f0430230471" />
<img src="https://github.com/user-attachments/assets/bdcd777e-2dda-4a1b-94b0-8cb113a59ac0" />
<img src="https://github.com/user-attachments/assets/b9402f99-a82d-4163-8d27-4be4eef6812c" />
<img src="https://github.com/user-attachments/assets/b582757d-824a-4f61-8410-8451b158e1ad" />
<img src="https://github.com/user-attachments/assets/4d9c962d-33bd-4de8-86b7-77e55506312c" />
<img src="https://github.com/user-attachments/assets/8ccccdc7-25fe-4847-8bbd-76e34c9a8af4" />
<img src="https://github.com/user-attachments/assets/54b074bc-b101-4813-a54c-db6030c81c4a" />
<img src="https://github.com/user-attachments/assets/397e97b8-8b01-4261-bd44-16aa1614667b" />
<img src="https://github.com/user-attachments/assets/b8ac0f6a-7481-4851-9f3b-b229a4c5f059" />
</p>

You can select the gradient you want statically or via a convenience method.

```java
HeatMapGradient gradient = HeatMapGradient.EXTENDED_GRADIENT;
gradient = HeatMapGradient.getCannedGradient(7);
```

You can also choose to great your own custom gradient. There are two choices, a smooth gradient based on the Hue, Saturation, Brightness (HSB) color model, or a stepped gradient making use of discrete colour steps.

**Smooth Gradients**

```java
HeatMapGradient mySmoothGradient = new HeatMapGradient(90, 270, 0.75f, 1.0f, false);
```

A smooth gradient is created by setting the following parameters:

|Attribute|Description|
|-|-|
|hueStart|The starting hue for the colour gradient, in degrees, between zero and 360 (inclusive).|
|hueEnd|The ending hue for the colour gradient, in degrees, between zero and 360 (inclusive).|
|saturation|The saturation of the colours, as a fractional value between zero and one.|
|brightness|The brightness of the colours, as a fractional value between zero and one.|
|clockwise|Whether the hue should be applied clockwise (true) or counter-clockwise (false).|

The gradient is created by starting at `hueStart` for the lowest value and rotating around the colour wheel to the `hueEnd` for the highest value. Since there are two possible ways to rotate around the colour wheel, the `clockwise` parameter is used to define the direction of rotation.

<p align="center">
  <img src="https://github.com/user-attachments/assets/f18c0cbd-fae9-4f11-a42f-1e6174d67f9a" width="400" />
</p>

**Stepped Gradients**

Defining a stepped gradient is even simpler, just create an array of `Color` objects.

```java
HeatMapGradient mySteppedGradient = new HeatMapGradient(new Color[] {
  Color.decode("#1d4877"), //Dark Blue
  Color.decode("#1b8a5a"), //Green
  Color.decode("#fbb021"), //Golden Yellow
  Color.decode("#f68838"), //Orange
  Color.decode("#ee3e32")  //Red
});
```

## Colour Blending

Enabling the `blendColours` option will result in a linear colour interpolation being applied to the cells of the heat map in order to "smooth out" the values. This effect works by first rendering each cell as a single pixel, then applying a bilinear upscaler (with a variable scaling factor), followed by applying an alpha mask to avoid blending with empty cells, and finally applying a nearest-neighbour up or down scaling to the final output resolution. The strength of this effect can be controlled via the `blendColoursScale` option, which supports values between 2 and 20, inclusive. By default, a value of 3 is used which will upscale each cell to a 3x3 grid, ensuring that the centre of the grid always represents the true colour of the corresponding data. Here is an example of what colour blending looks like, starting with no effect, then a strength of 2, 3, 5,and 10, respectively.

<p align="center">
  <img src="https://github.com/user-attachments/assets/f64e9520-7f8b-4473-be04-34be30771b69" width="200" />
  <img src="https://github.com/user-attachments/assets/1b3cdab1-97a9-4e4a-9f47-670644505911" width="200" />
  <img src="https://github.com/user-attachments/assets/6ecc18c4-0482-4f50-ad7d-a1f0d5dbf9f7" width="200" />
  <img src="https://github.com/user-attachments/assets/259f2575-b1f9-4aa1-9962-969fa3b6ed7c" width="200" />
  <img src="https://github.com/user-attachments/assets/657d4e69-3a81-47a2-84ca-e1bbafcbd57c" width="200" />
</p>

## Implementation Details

This library is built from scratch using the Java 2D graphics classes. To make it easy to integrate with your project, there are no external dependencies, only Java 8 or later. Because of the nature of the rendering, the calculations for each subsequent element of the chart depends on the calculations for the previous elements. This makes the code heavily procedural.   

**Implementation Notes**
- The colour of the background, when not explicitly set, is automatically determined. It will be set to white, Except if the byte value of all of the components of the colour for the maximum value (upper bound) of the gradient exceeds 240 (out of 255), in which case the background is set to a neutral grey `new Color(210, 210, 210)`. This is to prevent confusion with cells that have no value. Note that this does not check if any part of the gradient is close to white, only if the colour for the maximum value is. If you have white somewhere in the middle of your gradient, you should manually set the background colour to avoid conflicts.
- The cell width and heights are automatically determined using the maximum of: (1) the explicitly set dimensions, (2) the height of the font used for the axis labels, if they are being rendered, and (3) the dimensions of the largest (in pixels) grid value, once rendered, if rendering of grid values is enabled. 
- If not explicitly forced, the X-axis labels will be automatically rotated if the length of the longest label, plus the label padding, exceeds the cell width plus the the grid line thickness.
- The number of discrete steps of the legend, if not explicitly set, is determined by the greater of (1) the number of rows defined by the Y-axis, and (2) a hard coded value of 5.
- The legend is automatically vertically centred with the heat map grid, regardless which one is taller.
- The legend labels will automatically adjust to indicate if the bounds have been clamped by adding `>=` to the label for upper bound and `<=` to the label for the lower bound.
- The legend boxes are the same size as the main heat map grid cells and they use the same grid line options as the main chart. However, unlike the main heat map grid, they always render with an outside border. 
- The chart title is automatically line wrapped if, once rendered, it would exceed the width of the chart. This wrapping behaviour only applies to the main chart title and not the axis titles. This is something I plan to add eventually.

## Requirements

- Requires Java 8 or later.
- If you want to build the jar from the source code you will need [Apache Maven](https://maven.apache.org/).

## Legal Stuff

Copyright (c) 2024 David Fortin

This software is provided by David Fortin under the MIT License, meaning you are free to use it however you want, as long as you include the original copyright notice (above) and license notice in any copy you make. You just can't hold me liable in case something goes wrong. License details can be read [here](https://github.com/dbeaudoinfortin/heatmaps?tab=MIT-1-ov-file)
