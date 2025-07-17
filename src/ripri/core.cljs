(ns ripri.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

;; Simple state management without external dependencies
(defonce state (r/atom {:project-name ""
                        :units {:effort "person weeks", :reach "people", :reach-time "month"}
                        :rows {0 {:name "Build the ultra tech" :reach "5000" :impact "1" :confidence "80" :effort "4"}
                              1 {:name "Publish a blog post" :reach "100" :impact "1" :confidence "100" :effort "1"}
                              2 {:name "Release the hounds" :reach "10" :impact "2" :confidence "50" :effort "2"}}}))

;; URL state management for sharing
(defn encode-state [state]
  (-> state
      clj->js
      js/JSON.stringify
      js/btoa
      (clojure.string/replace #"\+" "-")
      (clojure.string/replace #"/" "_")
      (clojure.string/replace #"=" "")))

(defn decode-state [encoded]
  (try
    (-> encoded
        (clojure.string/replace #"-" "+")
        (clojure.string/replace #"_" "/")
        (#(str % (apply str (repeat (mod (count %) 4) "="))))
        js/atob
        js/JSON.parse
        (js->clj :keywordize-keys true))
    (catch :default e
      nil)))

(defn update-url 
  (let [encoded (encode-state @state)
        new-url (str (.-origin js/location) (.-pathname js/location) "?data=" encoded)]
    (js/history.replaceState nil "" new-url)))

(defn load-from-url []
  (let [url-params (js/URLSearchParams. (.-search js/location))
        data (.get url-params "data")]
    (when data
      (let [decoded (decode-state data)]
        (when decoded
          (reset! state decoded))))))

;; RICE calculation functions
(defn compute-score [row]
  (let [reach (js/parseFloat (:reach row))
        impact (js/parseFloat (:impact row))
        confidence (js/parseFloat (:confidence row))
        effort (js/parseFloat (:effort row))]
    (if (and (not (js/isNaN reach)) (not (js/isNaN impact)) 
             (not (js/isNaN confidence)) (not (js/isNaN effort)))
      (/ (* reach impact confidence) effort)
     0)))

(defn sort-rows [rows]
  (into {}
        (->> rows
             (sort-by (fn [[idx v]] (compute-score v)))
             reverse
             (map-indexed (fn [idx [k v]] {idx v})))))

;; Event handlers
(defn make-change-handler [coords]
  (fn [event]
    (let [value (.-value (.-target event))]
      (swap! state assoc-in coords value)
      (update-url state))))

(defn add-row []
  (let [new-id (inc (count (:rows @state)))]
    (swap! state assoc-in [:rows new-id] 
         {:name "" :reach "" :impact "1" :confidence "80" :effort ""})
    (update-url state)))

(defn delete-row [row-id]
  (swap! state update :rows dissoc row-id)
  (update-url state))

(defn sort-by-priority []
  (swap! state update :rows sort-rows)
  (update-url state))

;; Export/Import functionality
(defn export-data []
  (let [data (clj->js @state)
        blob (js/Blob. [(js/JSON.stringify data nil 2)] #js {:type "application/json"})
        url (js/URL.createObjectURL blob)
        a (js/document.createElement "a")]
    (set! (.-href a) url)
    (set! (.-download a) "rice-projects.json")    (.click a)
    (js/URL.revokeObjectURL url)))

(defn import-data [event]
  (let [file (first (.-files (.-target event)))]
    (when file
      (let [reader (js/FileReader.)]
        (set! (.-onload reader) 
              (fn [e]
                (try
                  (let [data (js->clj (js/JSON.parse (.-result e)) :keywordize-keys true)]
                    (reset! state data)
                    (update-url state))
                  (catch :default e
                    (js/alert "Invalid file format")))))
        (.readAsText reader file)))))

;; UI Components
(defn input-field [coords placeholder & [props]]
  [:input (merge {:value (get-in @state coords)
                  :on-change (make-change-handler coords)
                  :placeholder placeholder}
                 props)])

(defn select-field [coords options]
  [:select {:value (get-in @state coords)
            :on-change (make-change-handler coords)}
   (for [[value label] options]
     [:option {:key value :value value} label])])

(defn project-table []
  [:table
   [:thead
    [:tr
     [:th "Name"]
     [:th "Reach"]
     [:th "Impact"]
     [:th "Confidence"]
     [:th "Effort"]
     [:th "Score"]
     [:th "Actions"]]]
   [:tbody
    (for [[row-id row] (:rows @state)]
      [:tr {:key row-id}
       [:td [input-field [:rows row-id :name] "Project name..." {:style {:width 200}}]
       [:td [input-field [:rows row-id :reach] "1000" {:type "number" :min "0" :style {:width 80}}]
       [:td [select-field [:rows row-id :impact] 
             [[3 "Huge"] [2 "High"] [1 "Mid"] [0.5 "Low"] [0.25 "Tiny"]]]]
       [:td [select-field [:rows row-id :confidence] 
             [[100 "High"] ["80 Mid"] ["50 Low"]]]]
       [:td [input-field [:rows row-id :effort] "1" {:type "number" :min "0.1" :style {:width 80}}]
       [:td {:style {:font-weight "bold" :color "#2563b8"}} 
        (.toFixed (compute-score row) 0)
        [:td [:button {:on-click #(delete-row row-id) :style {:background "#dc2626"}} "×"]]]))]]

(defn main-app []
  [:div {:style {:max-width 1200 :margin "0 auto" :padding "20px"}}
   [:header {:style {:text-align "center" :margin-bottom 40}}
    [:h1 "RICE Prioritization Framework"]
    [:p "Prioritize your projects using the RICE scoring method: (Reach × Impact × Confidence) ÷ Effort"]]
   
   [:div {:style {:background "#ffffff" :padding "30px" :border-radius 8 :box-shadow "0 4px 6px rgba(0,0,0,0.1)"}}
    [:div {:style {:margin-bottom "20px"}} [:label "Project Name: "]
     [input-field [:project-name] "Enter project name..." {:style {:width 300}}]
    [:div {:style {:margin-bottom "20px"}}
     [:label "Effort in "]
     [select-field [:units :effort] 
     ["person months" "person months"] ["person weeks" "person weeks"] ["person days" "person days"]]]
     [:label " | Reach in "]
     [input-field [:units :reach] "people" {:style {:width 100}}]
     [:label " per "]
     [select-field [:units :reach-time] 
    ["year" "year"] ["month" "month"] ["week" "week"] ["day" "day"]]]]
    
    [project-table]
    
    [:div {:style {:margin-top 20 :display "flex" :gap "10px" :justify-content "flex-end"}}
     [:button {:on-click add-row} "Add Project"]
     [:button {:on-click sort-by-priority} "Sort by Priority"]
     [:button {:on-click export-data} "Export"]    [:input {:type "file" :accept ".json" :on-change import-data :style {:display "none"} :id "import-input"}]
     [:button {:on-click #(.click (js/document.getElementById "import-input"))} "Import"]]]
    
    [:div {:style {:margin-top 30 :padding 20 :background "#f8f9fa" :border-radius 8}}
     [:h3 "Share this URL to save your data:"]    [:input {:type "text" :value (str (.-origin js/location) (.-pathname js/location) "?data=" (encode-state @state))
              :read-only true :style {:width 100 :padding 10 :border 1px solid "#ddd" :border-radius 4}}
     [:p {:style {:font-size "14px" :color "#666"}} "Copy this URL to share your RICE calculations with others or save for later."]]]
   
   [:footer {:style {:text-align "center" :margin-top 40 :color "#666"}}
    [:p "Made with 🤖 by " [:a {:href "https://twitter.com/mccrmx" :target "_blank"} "@mccrmx"]]
    [:p [:a {:href "https://github.com/chr15m/riceprioritization.com"} "Source Code"]]]])

;; Initialize app
(defn init []
  ;; Remove loading spinner
  (let [loading-el (js/document.getElementById "loading")]
    (when loading-el
      (.remove loading-el)))
  
  ;; Load data from URL if present
  (load-from-url)
  
  ;; Render the app
  (rdom/render [main-app] (js/document.getElementById "app))  
  ;; Update URL with initial state
  (update-url state))

;; Hot reload support
(defn ^:dev/after-load reload []
  (init))
