import Foundation
import UIKit
import youtube_ios_player_helper
import KotlinConfAPI

class LiveVideo : UICollectionViewCell {
    @IBOutlet weak var sessionTitle: UILabel!
    @IBOutlet weak var speaker: UILabel!
    @IBOutlet weak var video: YTPlayerView!
    @IBOutlet weak var location: UILabel!
    @IBOutlet weak var favoriteButton: UIButton!

    var favoriteObservable: Ktor_ioCloseable? = nil

    var card: SessionCard! {
        didSet {
            favoriteObservable?.close()

            sessionTitle.text = card.session.title
            speaker.text = card.speakers.map { (speaker) -> String in
                speaker.fullName
            }.joined(separator: ", ")

            location.text = card.location.name
            video.load(withVideoId: "YbF8Q8LxAJs")

            favoriteObservable = card.isFavorite.watch(block: { isFavorite in
                self.favoriteButton.isSelected = isFavorite!.boolValue
            })
        }
    }

    @IBAction func favoriteTouch(_ sender: Any) {
        Conference.markFavorite(sessionId: card.session.id)
    }
}
