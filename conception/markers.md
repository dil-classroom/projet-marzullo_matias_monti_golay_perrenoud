# Meta-datas
## From config file :
These metas are in the file named "config.json"  
Regex : "/\\[\\[ config.\S+ ]]/gi"  
Example : [[ config.site_name ]]  
Usage : In the config file, use the field name "site_name" to replace "[[ config.site_name ]]"  
if the name is "site.name", it means to use the sub-field named "name" from the field "site".

## From page file :
These metas are written on top of the MD file  
Regex : "/\\[\\[ page.\S+ ]]/gi"  
No sub-fields here  
Working like meta-data from config

# File inclusion :
Regex : "/\\[\\[\\+ '.+\.html' ]]/gi"  
Example : [[+ 'file/stuff/include.html' ]]  
Usage : Copy the content of 'file/stuff/include.html' at the place of [[+ ... ]]

# Content Inclusion
Regex : "/\\[\\[ content ]]/gi"  
Example : [[ content ]]  
Usage : Place the content of the MD to replace "[[ content ]]"
