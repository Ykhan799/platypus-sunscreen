package com.example.sunscreen.sensing.sensors

class CameraSensor {
    // NB image brightness (Y == luminance == brightness) doesn't correlate with light intensity,
    // and even less so with UV levels. It's more of just how black or white the image is.
    // Using the camera may not provide any help, barring some sort of image recognition to look up
    // and determine if it's looking at sky or ceiling.
}