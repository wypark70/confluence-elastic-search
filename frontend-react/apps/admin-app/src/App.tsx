import Button from '@atlaskit/button/new';
import { useState } from 'react'
import './App.css'
import Avatar from '@atlaskit/avatar';
import Comment, { CommentAuthor, CommentEdited, CommentTime } from '@atlaskit/comment';

const sampleAvatar = 'https://bit.ly/dan-abramov';

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
      <h1>Atlassian Design System - Admin</h1>
		<Comment
			avatar={<Avatar name="Scott Farquhar" src={sampleAvatar} />}
			author={<CommentAuthor>Scott Farquhar</CommentAuthor>}
			edited={<CommentEdited>Edited</CommentEdited>}
			time={<CommentTime>Jul 3, 2020</CommentTime>}
			content={
				<p>
					I'm super proud that 69% of our almost 5,000 Atlassian employees donated their time for
					volunteering in the last year. Thanks team!
				</p>
			}
		/>
      <div className="card">
        <Button appearance="warning" onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </Button>
        <p>
          Edit <code>src/App.tsx</code> and save to test HMR
        </p>
      </div>
    </>
  )
}

export default App
