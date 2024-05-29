#!/bin/bash

echo $1
echo $2
find "$PWD" -name "$1*" -exec rename -v 's/$1/$2/g' {} \;