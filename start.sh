tmux new-session -d -s viime
tmux split-window -t viime:1 -v
tmux rename-window main
tmux send-keys -t viime:1.1 "nvim src/cljs/viime/core.cljs" "Enter"
tmux send-keys -t viime:1.2 "lein repl" "Enter"
tmux new-window -t viime:2
tmux select-window -t viime:2
tmux rename-window server
tmux select-window -t viime:1
tmux attach -t viime
