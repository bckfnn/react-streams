package com.github.bckfnn.docs;

public class FlowDiag {

    /**
     * import json

x = {
        "input" : [
            { "type" : "line" },
            { "pos" : 0, "type":"circle", "color":"red"},
            { "pos" : 1, "type":"circle", "color":"green"},
            { "pos" : 2, "type":"circle", "color":"blue"},
            { "pos" : 3, "type":"end", },
        ],
        "code":"code",
        "output" : [
            { "pos" : 0, "type":"rhomb", "color":"red"},
            { "pos" : 1, "type":"rhomb", "color":"green"},
            { "pos" : 2, "type":"rhomb", "color":"blue"},
            { "pos" : 3, "type":"end", },
        ]
    }


colors = {
    "red" : "#FF0000",
    "green" : "#00FF00",
    "blue" : "#0000FF",
}
i = 1

def symbols(lst):
    global i
    pos = 100
    for fig in x["input"]:
        fig["id"] = i
        fig["pos"] = pos
        if fig.has_key("color"):
            fig["color"] = colors[fig["color"]]
        pos += 30
        i += 1
        if fig["type"] == "circle":
            print >>f, circle % fig
        if fig["type"] == "line":
            fig["start"] = fig["pos"] + pos
            fig["end"] = fig["start"] + fig["len"]

            print >>f, line & fig



circle = """  <ellipse transform="null" filter="url(#svg_3_blur)" ry="10" rx="10" id="svg_%(id)d" cy="100" cx="%(pos)d" stroke-linecap="null" stroke-linejoin="null" stroke-dasharray="null" stroke-width="2" stroke="#000000" fill="%(color)s"/>"""

line = """ <line id="svg_%(id)s" y2="100" x2="%(end)d" y1="100" x1="%(start)d" stroke-linecap="null" stroke-linejoin="null" stroke-dasharray="null" stroke-width="3" stroke="#000000" fill="none"/>"""

head =  """
<svg width="640" height="480" xmlns="http://www.w3.org/2000/svg" xmlns:svg="http://www.w3.org/2000/svg">
 <!-- Created with SVG-edit - http://svg-edit.googlecode.com/ -->
 <defs>
  <filter id="svg_3_blur">
   <feGaussianBlur stdDeviation="0.1" in="SourceGraphic"/>
  </filter>
 </defs>
 <g>
  <title>Layer 1</title>
"""
foot = """
  </g>
</svg>
"""

f = open("xx.svg", "w")
print >>f, head
symbols(x["input"])
print >>f, foot


     */
}
