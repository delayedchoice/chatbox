{:foreign-libs
   [{:file "easyrtc/socket.io.js"
         ;:file-min "react/react.min.js"
             :provides ["socket.io.js"]} ]
   [{:file "easyrtc/easyrtc.js"
        ; :file-min "react/react.min.js"
             :provides ["easyrtc.js"]
             :requires ["socket.io.js"]}
             ]
    :externs ["easyrtc/socket.io.js" "easyrtc/easyrtc.js"]}
