#!/bin/bash

find . -name "$1*" -exec sed -i -e 's/24.0dip/36.0dp/g' {} \;
find . -name "$1*" -exec sed -i -e 's/24.0dp/36.0dp/g' {} \;
find "$PWD" -name "$1*" -exec sed -i -e 's/24.0dip/36.0dp/g' {} \;
find "$PWD" -name "$1*" -exec sed -i -e 's/24.00dp/36.0dp/g' {} \;
find "$PWD" -name "$1*" -exec sed -i -e 's/24dp/36.0dp/g' {} \;